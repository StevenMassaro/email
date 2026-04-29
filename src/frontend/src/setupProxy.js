const {createProxyMiddleware} = require('http-proxy-middleware');

const backendTarget = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

module.exports = function (app) {
  app.use(
    '/message',
    createProxyMiddleware({target: backendTarget, changeOrigin: true})
  );
  app.use(
    '/body',
    createProxyMiddleware({target: backendTarget, changeOrigin: true})
  );
  app.use(
    '/attachment',
    createProxyMiddleware({target: backendTarget, changeOrigin: true})
  );
  app.use(
    '/actions',
    createProxyMiddleware({target: backendTarget, changeOrigin: true})
  );
};
