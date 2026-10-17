config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ['/warsaw-api'],
        target: 'https://dane.um.warszawa.pl',
        changeOrigin: true,
        secure: true,
        pathRewrite: { '^/warsaw-api': '' },
    },
];
