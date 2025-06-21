const express = require('express');
const app = express();
const PORT = process.env.PORT || 3002;

app.use(express.json());

app.get('/', (req, res) => {
  res.json({ service: 'inventory-service' });
});

app.listen(PORT, () => {
  console.log(`inventory-service listening on port ${PORT}`);
});
