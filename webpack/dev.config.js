const webpack = require("webpack");
const constants = require('./constants');

const devServer = {
    host: 'localhost',
    port: 3001,

    // enable the HMR on the server
    hot: true,

    headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
        "Access-Control-Allow-Headers": "X-Requested-With, content-type, Authorization"
    },

    compress: true,
    overlay: true
};

const devServerURL = 'http://' + devServer.host + ':' + devServer.port;

module.exports = {
    context: constants.context(__dirname),

     devtool: 'inline-source-map',
    //devtool: 'eval',

    devServer: devServer,

    entry: {
        'ontologyman': [
            'react-hot-loader/patch',
            'webpack-dev-server/client?' + devServerURL,
            'webpack/hot/only-dev-server',

            './src/client/views/ontologyman.jsx'
        ],
        'ontologyoverview': [
            'react-hot-loader/patch',
            'webpack-dev-server/client?' + devServerURL,

            'webpack/hot/only-dev-server',

            './src/client/views/ontologyoverview.jsx'
        ],
    },

    output: {
        path: constants.outputPath(__dirname),
        publicPath: devServerURL + '/',
        filename: "[name].js"
    },


    resolve: {
        extensions: constants.extensions.JAVASCRIPT
    },

    module: {
        loaders: constants.loaders.JAVASCRIPT_LOADERS_DEV
    },

    plugins: [
        // enable HMR globally
        new webpack.HotModuleReplacementPlugin(),

        // prints more readable modules names in the browser console on HMR updates
        new webpack.NamedModulesPlugin(),

        // do not emit compiled assets that include errors
        new webpack.NoEmitOnErrorsPlugin()
    ]
};
