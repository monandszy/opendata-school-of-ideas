config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ['/warsaw-api'],
        target: 'https://dane.um.warszawa.pl',
        changeOrigin: true,
        secure: true,
        pathRewrite: { '^/warsaw-api': '' },
    },
    {
        context: ['/sensor-community-api'],
        target: 'https://data.sensor.community',
        changeOrigin: true,
        secure: true,
        pathRewrite: { '^/sensor-community-api': '' },
    },
];
