const fs = require('fs');
const path = require('path');

const envPath = path.join(__dirname, 'src', 'environments', 'environment.prod.ts');

const apiUrl = process.env.API_URL || 'http://localhost:8083/api';
const wsUrl = process.env.WS_URL || 'http://localhost:8083/ws';

const content = `export const environment = {
  production: true,
  apiUrl: '${apiUrl}',
  wsUrl: '${wsUrl}'
};
`;

try {
  fs.writeFileSync(envPath, content, { encoding: 'utf8' });
  console.log(`[set-env] Successfully generated environment.prod.ts:`);
  console.log(`          API_URL = ${apiUrl}`);
  console.log(`          WS_URL  = ${wsUrl}`);
} catch (error) {
  console.error('[set-env] Error generating environment.prod.ts:', error);
  process.exit(1);
}
