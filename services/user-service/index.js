const express = require('express');
const app = express();
const PORT = process.env.PORT || 3001;

app.use(express.json());

app.get('/users', (req, res) => {
  res.json({ message: 'User service root' });
});

app.listen(PORT, () => {
  console.log(`User service listening on port ${PORT}`);
});
