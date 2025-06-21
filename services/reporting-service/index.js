const express = require('express');
const app = express();
const PORT = process.env.PORT || 3004;

app.use(express.json());

app.get('/', (req, res) => {
  res.json({ service: 'reporting-service' });
});

app.listen(PORT, () => {
  console.log(`reporting-service listening on port ${PORT}`);
});
