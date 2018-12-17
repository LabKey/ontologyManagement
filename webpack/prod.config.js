
const webpack = require("webpack");
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const constants = require("./constants");

module.exports = {
    context: constants.context(__dirname),

    devtool: 'source-map',

    entry: {
        'ontologyman': [
           './src/theme/style.js',
           './src/client/views/ontologyman.jsx'
        ],'ontologyoverview': [
            './src/theme/style.js',
            './src/client/views/ontologyoverview.jsx'
         ]
    },

    output: {
        path: constants.outputPath(__dirname),
        publicPath: './', // allows context path to resolve in both js/css
        filename: "[name].js"
    },

    module: {
        rules: constants.loaders.STYLE_LOADERS.concat(constants.loaders.JAVASCRIPT_LOADERS)
    },

    resolve: {
        extensions: constants.extensions.JAVASCRIPT
    },

    plugins: [
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': '"development"'
        }),
        new ExtractTextPlugin({
            allChunks: true,
            filename: '[name].css'
        }),
        new webpack.optimize.UglifyJsPlugin({
            sourceMap: true
        })
    ]
};

