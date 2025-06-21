const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

app.get('/', (req, res) => {
  res.json({ service: 'gateway-service' });
});

app.listen(PORT, () => {
  console.log(`gateway-service listening on port ${PORT}`);
});
