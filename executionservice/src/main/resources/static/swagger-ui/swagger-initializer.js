window.onload = function() {
  // swagger-ui 5 ignores the ?url= query parameter, so the AJAN spec is configured here.
  window.ui = SwaggerUIBundle({
    url: "/ajan/swagger.json",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });
};
