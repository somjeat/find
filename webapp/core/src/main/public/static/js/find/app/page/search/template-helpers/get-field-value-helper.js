/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(fieldId) {
        const field = _.findWhere(this.fields, {id: fieldId});
        return field ? field.values[0] : undefined;
    };

});
