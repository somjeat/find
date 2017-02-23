/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    './saved-search-widget',
    'find/app/model/dependent-parametric-collection',
    'parametric-refinement/prettify-field-name',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-widget-legend.html',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-widget-legend-item.html',
    'i18n!find/nls/bundle'
], function(_, $, d3, Sunburst, SavedSearchWidget, DependentParametricCollection,
            prettifyFieldName, legendTemplate, legendItemTemplate, i18n) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'sunburst',
        legendTemplate: _.template(legendTemplate),
        legendItemTemplate: _.template(legendItemTemplate),

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            // TODO display error msg if field absent (no dashboards config validation)
            this.firstField = options.widgetSettings.firstField;
            this.secondField = options.widgetSettings.secondField;
            this.dependentParametricCollection = new DependentParametricCollection({
                minShownResults: 10
            });
        },

        postInitialize: function() {
        },

        onResize: function() {
        },

        getData: function() {
            var promise;
            if(this.firstField) {
                promise = this.dependentParametricCollection
                    .fetchDependentFields(this.queryModel, this.firstField, this.secondField);
            }

            return promise;
        }
    });
});
