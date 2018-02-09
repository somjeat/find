/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/substitution'
], function(substitution) {
    'use strict';

    // Caution: only use this file for errors that the user can understand and solve. If an error is listed here, by default the app will not advise the user to contact support.
    return substitution({
        // TODO: Copypasted from userErrors in IdolGlobalExceptionHandler.java to enable lookup by
        // generate-error-support-message.js. This is used on the frontend to decide if it is
        // a user error and if a 'contact support' message should be displayed. Establish what these errors mean and
        // provide prettified user messages.
        // 'error.code.AXEQUERY504': '',
        // 'error.code.DAHQUERY504': '',
        // 'error.code.AXEQUERY505': '',
        // 'error.code.DAHQUERY505': '',
        // 'error.code.AXEQUERY507': '',
        // 'error.code.DAHQUERY507': '',
        // 'error.code.AXEQUERY508': '',
        // 'error.code.DAHQUERY508': '',
        // 'error.code.AXEQUERY509': '',
        // 'error.code.DAHQUERY509': '',
        // 'error.code.AXEQUERY511': '',
        // 'error.code.DAHQUERY511': '',
        // 'error.code.AXEQUERY513': '',
        // 'error.code.DAHQUERY513': '',
        // 'error.code.AXEGETQUERYTAGVALUES507': '',
        // 'error.code.DAHGETQUERYTAGVALUES507': '',
        // 'error.code.AXEGETQUERYTAGVALUES508': '',
        // 'error.code.DAHGETQUERYTAGVALUES508': '',
        // 'error.code.AXEGETQUERYTAGVALUES519': '',
        // 'error.code.DAHGETQUERYTAGVALUES519': '',
        // 'error.code.AXEGETQUERYTAGVALUES520': '',
        // 'error.code.DAHGETQUERYTAGVALUES520': '',
        // 'error.code.AXEGETQUERYTAGVALUES522': '',
        // 'error.code.DAHGETQUERYTAGVALUES522': '',
        // 'error.code.AXEGETQUERYTAGVALUES538': '',
        // 'error.code.DAHGETQUERYTAGVALUES538': '',
        // 'error.code.QMSQUERY-2147435967': '',
        // 'error.code.QMSQUERY-2147435888': '',
        // 'error.code.QMSGETQUERYTAGVALUES-2147483377': '',
        // 'error.code.AXEGETQUERYTAGVALUES504': '',
        // 'error.code.DAHGETQUERYTAGVALUES504': '',
        // 'error.code.AXEGETQUERYTAGVALUES509': '',
        // 'error.code.DAHGETQUERYTAGVALUES509': '',
        // 'error.code.AXEGETQUERYTAGVALUES513': '',
        // 'error.code.DAHGETQUERYTAGVALUES513': '',
        'error.code.DAHGETQUERYTAGVALUES502': 'รูปแบบข้อความไม่ถูกต้อง: เป็นคำย่อ, คำสั้นไป, หรือรูปแบบไม่ถูกต้อง',
        'error.code.AXEGETQUERYTAGVALUES502': 'รูปแบบข้อความไม่ถูกต้อง: เป็นคำย่อ, คำสั้นไป, หรือรูปแบบไม่ถูกต้อง',
        'error.code.DAHQUERY502': 'รูปแบบข้อความไม่ถูกต้อง: เป็นคำย่อ, คำสั้นไป, หรือรูปแบบไม่ถูกต้อง',
        'error.code.AXEQUERY502': 'รูปแบบข้อความไม่ถูกต้อง: เป็นคำย่อ, คำสั้นไป, หรือรูปแบบไม่ถูกต้อง',
        'error.code.DAHGETQUERYTAGVALUES512': 'ระบบค้นหาไม่เข้าใจคำในการค้นหาของคุณ',
        'error.code.AXEGETQUERYTAGVALUES512': 'ระบบค้นหาไม่เข้าใจคำในการค้นหาของคุณ',
        'error.code.DAHQUERY512': 'ระบบค้นหาไม่เข้าใจคำในการค้นหาของคุณ',
        'error.code.AXEQUERY512': 'ระบบค้นหาไม่เข้าใจคำในการค้นหาของคุณ'
    });
});
