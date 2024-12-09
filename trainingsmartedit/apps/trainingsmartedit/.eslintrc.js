/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
module.exports = {
    extends: './node_modules/smartedit-build/config/.eslintrc.js',
    parserOptions: {
        tsconfigRootDir: __dirname
    },
    overrides: [
        {
            files: '{src,tests}/**/*.js',
            rules: {
                'no-var': 'off'
            }
        }
    ]
};
