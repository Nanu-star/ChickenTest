const express = require('express');
const app = express();
const PORT = process.env.PORT || 3003;

app.use(express.json());

app.get('/', (req, res) => {
  res.json({ service: 'transaction-service' });
});

app.listen(PORT, () => {
  console.log(`transaction-service listening on port ${PORT}`);
});
