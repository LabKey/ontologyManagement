const path = require("path");
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
    context: function(dir) {
        return path.resolve(dir, '..');
    },
    extensions: {
        JAVASCRIPT: [ '.jsx', '.js' ]
    },
    loaders: {
        STYLE_LOADERS: [
            {
                test: /\.css$/,
                loader: ExtractTextPlugin.extract({
                    use: 'css-loader',
                    fallback: 'style-loader'
                })
            },
            {
                test: /\.scss$/,
                loader: ExtractTextPlugin.extract({
                    use: [{
                        loader: 'css-loader',
                        options: {
                            importLoaders: 1
                        }
                    },{
                        loader: 'postcss-loader',
                        options: {
                            sourceMap: 'inline'
                        }
                    },{
                        loader: 'resolve-url-loader'
                    },{
                        loader: 'sass-loader',
                        options: {
                            sourceMap: true
                        }
                    }],
                    fallback: 'style-loader'
                })
            },

            { test: /\.woff(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=application/font-woff" },
            { test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=application/font-woff" },
            { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=application/octet-stream" },
            { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file-loader" },
            { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=image/svg+xml" },
            { test: /\.png(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=image/png" },

            {
                test: /style.js/,
                loaders: [{
                    loader: 'babel-loader',
                    options: {
                        cacheDirectory: true
                    }
                }]
            }
        ],
        JAVASCRIPT_LOADERS: [
            {
                test: /^(?!.*spec\.jsx?$).*\.jsx?$/,
                loaders: [{
                    loader: 'babel-loader',
                    options: {
                        presets: ["env", "react"],
                        cacheDirectory: true
                    }
                }]
            }
        ],
        JAVASCRIPT_LOADERS_DEV: [
            {
                test: /^(?!.*spec\.jsx?$).*\.jsx?$/,
                loaders: [{
                    loader: 'babel-loader',
                    options: {
                        cacheDirectory: true,
                        presets: [["env", {"modules": false}], "react"],
                        plugins: ["react-hot-loader/babel"]
                    }
                }]
            }
        ]
    },
    outputPath: function(dir) {
        return path.resolve(dir, '../resources/web/nestle/gen');
    }
};