const {createProxyMiddleware} = require('http-proxy-middleware');

require('dotenv').config();

const opts = {
  target: process.env.LIGHTER_DEV_PROXY_URL || 'http://localhost:8080',
  secure: true,
  cookieDomainRewrite: 'localhost',
  preserveHeaderKeyCase: true,
  changeOrigin: true,
};

module.exports = function (app) {
  app.use(createProxyMiddleware('/lighter/api', opts));
};
