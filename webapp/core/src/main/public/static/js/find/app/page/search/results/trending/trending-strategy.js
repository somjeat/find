/*
 *  Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 *  Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'moment',
    'd3',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/vent',
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/field-selection-view',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/model/bucketed-date-collection',
    'find/app/model/date-field-details-model',
    'find/app/model/parametric-collection',
    'find/app/page/search/results/trending/trending',
    'parametric-refinement/to-field-text-node',
], function(_, $, moment, d3, Backbone, i18n, configuration, vent, ParametricResultsView,
            FieldSelectionView, calibrateBuckets, BucketedParametricCollection, ParametricDetailsModel,
            ParametricCollection, Trending, toFieldTextNode) {
    'use strict';

    const HOURS_TO_SECONDS = 3600;
    const MINUTES_TO_SECONDS = 60;
    const DAYS_TO_SECONDS = 86400;
    const YEARS_TO_SECONDS = 31536000;

    function fetchField(options) {
        const trendingCollection = new ParametricCollection([], {url: 'api/public/parametric/values'});
        return trendingCollection.fetch({
            data: {
                fieldNames: [options.field],
                databases: options.queryModel.get('indexes'),
                queryText: options.queryModel.get('autoCorrect') && options.queryModel.get('correctedQuery')
                    ? options.queryModel.get('correctedQuery')
                    : options.queryModel.get('queryText'),
                fieldText: toFieldTextNode(getFieldText(options.selectedParametricValues)),
                minDate: options.queryModel.getIsoDate('minDate'),
                maxDate: options.queryModel.getIsoDate('maxDate'),
                minScore: options.queryModel.get('minScore'),
                maxValues: options.values
                    ? null
                    : options.numberOfValuesToDisplay,
                valueRestrictions: options.values
                    ? _.pluck(options.values, 'name')
                    : null
            }
        }).then(function(fieldList) {
            const field = fieldList.filter(function(fieldData) {
                return fieldData.id === options.field;
            }, this);

            return field[0]
                ? field[0].values
                : [];
        });
    }

    function fetchRange(selectedFieldValues, options) {
        const trendingValues = _.first(selectedFieldValues, options.numberOfValuesToDisplay);
        const encodedValues = _.map(trendingValues, function(value) {
            return encodeURIComponent(value.value);
        });
        const trendingValuesRestriction = 'MATCH{' + encodedValues.toString() + '}:' + options.field;
        const fieldText = getFieldText(options.selectedParametricValues).length > 0
            ? ' AND ' + toFieldTextNode(getFieldText(options.selectedParametricValues))
            : '';

        return new ParametricDetailsModel().fetch({
            data: {
                fieldName: options.dateField,
                queryText: options.queryModel.get('queryText'),
                fieldText: trendingValuesRestriction + fieldText,
                minDate: options.queryModel.getIsoDate('minDate'),
                maxDate: options.queryModel.getIsoDate('maxDate'),
                minScore: options.queryModel.get('minScore'),
                databases: options.queryModel.get('indexes')
            }
        });
    }

    function fetchBucketedData(options) {
        const valuesToShow = _.first(options.selectedFieldValues, options.numberOfValuesToDisplay);

        const requests = _.map(valuesToShow, function(value) {
            const bucketModel = new BucketedParametricCollection.Model({
                id: options.dateField,
                valueName: value.value
            });

            const fieldText = getFieldText(options.selectedParametricValues).length > 0
                ? ' AND ' + toFieldTextNode(getFieldText(options.selectedParametricValues))
                : '';

            const xhr = bucketModel
                .fetch({
                    data: {
                        queryText: options.queryModel.get('queryText'),
                        fieldText: 'MATCH{' + encodeURIComponent(value.value) + '}:' + options.field + fieldText,
                        minDate: options.queryModel.getIsoDate('minDate'),
                        maxDate: options.queryModel.getIsoDate('maxDate'),
                        minScore: options.queryModel.get('minScore'),
                        databases: options.queryModel.get('indexes'),
                        targetNumberOfBuckets: options.targetNumberOfBuckets,
                        bucketMin: options.currentMin.toISOString(),
                        bucketMax: options.currentMax.toISOString()
                    }
                });

            const promise = xhr.then(function(data) {
                return _.extend(data, {
                    min: moment(data.min),
                    max: moment(data.max),
                    values: data.values.map(function(value) {
                        return _.extend(value, {min: moment(value.min), max: moment(value.max)});
                    }),
                    valueName: value.value,
                    color: options.values
                        ? _.findWhere(options.values, {'name': value.value}).color
                        : null
                });
            });

            return {promise: promise, xhr: xhr};
        });

        const promise = $.when.apply($, _.pluck(requests, 'promise')).promise();

        promise.abort = function() {
            requests.forEach(function(request) {
                request.xhr.abort();
            });
        };

        return promise;
    }

    function getFieldText(selectedParametricValues) {
        return selectedParametricValues.map(function(model) {
            return model.toJSON();
        });
    }

    function createChartData(options) {
        // Assume all buckets have the same width
        const firstPoint = options.bucketedValues[0].values[0];
        const bucketWidthSecs = firstPoint.bucketSize;
        const halfBucketWidthSecs = 0.5 * bucketWidthSecs;

        const flatCountsChain = _.chain(options.bucketedValues)
            .pluck('values')
            .map(_.partial(_.pluck, _, 'count'))
            .flatten();

        const minCount = flatCountsChain.min().value();
        const maxCount = flatCountsChain.max().value();
        const maxRatePerSec = maxCount / bucketWidthSecs;

        let yUnit;
        let rateCoefficient;

        if(maxRatePerSec >= 1) {
            yUnit = 'SECOND';
            rateCoefficient = 1 / bucketWidthSecs;
        } else if(maxRatePerSec >= (1 / MINUTES_TO_SECONDS)) {
            yUnit = 'MINUTE';
            rateCoefficient = MINUTES_TO_SECONDS / bucketWidthSecs;
        } else if(maxRatePerSec >= (1 / HOURS_TO_SECONDS)) {
            yUnit = 'HOUR';
            rateCoefficient = HOURS_TO_SECONDS / bucketWidthSecs;
        } else if(maxRatePerSec >= (1 / DAYS_TO_SECONDS)) {
            yUnit = 'DAY';
            rateCoefficient = DAYS_TO_SECONDS / bucketWidthSecs;
        } else {
            yUnit = 'YEAR';
            rateCoefficient = YEARS_TO_SECONDS / bucketWidthSecs;
        }

        const data = _.map(options.bucketedValues, function(bucketedValue) {
            return {
                name: bucketedValue.valueName,
                color: bucketedValue.color,
                points: _.chain(bucketedValue.values)
                    .map(function(value) {
                        const midTime = value.min.clone().add(Math.floor(halfBucketWidthSecs), 'seconds');

                        return {
                            rate: value.count * rateCoefficient,
                            mid: midTime.toDate(),
                            min: value.min.toDate(),
                            max: value.max.toDate()
                        };
                    })
                    .filter(function(value) {
                        const mid = moment(value.mid);
                        const currentMin = options.currentMin;
                        const currentMax = options.currentMax;
                        return mid.diff(currentMin) >= 0 && mid.diff(currentMax) <= 0;
                    })
                    .value()
            };
        });

        return {
            data: data,
            minRate: minCount * rateCoefficient,
            maxRate: maxCount * rateCoefficient,
            yUnit: yUnit
        };
    }

    return {
        fetchField: fetchField,
        fetchRange: fetchRange,
        fetchBucketedData: fetchBucketedData,
        createChartData: createChartData
    };
});
