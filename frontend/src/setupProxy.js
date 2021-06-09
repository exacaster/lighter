const {createProxyMiddleware} = require('http-proxy-middleware');

const opts = {
  target: 'https://stagecat.exacaster.com/',
  secure: true,
  cookieDomainRewrite: 'localhost',
  preserveHeaderKeyCase: true,
  changeOrigin: true,
};

module.exports = function (app) {
  app.use(createProxyMiddleware('/lighter/api', opts));
};
