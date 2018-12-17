var ExtractTextPlugin = require('extract-text-webpack-plugin');
const constants = require('./constants');

module.exports = {
    context: constants.context(__dirname),

    entry: {
        'app': [
            './src/theme/style.js'
        ]
    },

    output: {
        path: constants.outputPath(__dirname),
        publicPath: '/labkey/nestle/gen/',
        filename: 'style.js' // do not override app.js
    },

    module: {
        loaders: constants.loaders.STYLE_LOADERS
    },

    plugins: [
        new ExtractTextPlugin({
            allChunks: true,
            filename: '[name].css'
        })
    ]
};