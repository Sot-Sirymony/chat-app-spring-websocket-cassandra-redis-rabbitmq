const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

async function globalSetup() {
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 5000);
    const res = await fetch(BASE_URL + '/', { signal: controller.signal, redirect: 'manual' });
    clearTimeout(timeout);
    if (res.status !== 200 && res.status !== 302) {
      throw new Error('Backend returned ' + res.status);
    }
  } catch (e) {
    console.error('\n*** E2E tests require the Spring Boot backend at ' + BASE_URL + ' ***');
    console.error('Start it with: cd ebook-chat && mvn spring-boot:run');
    console.error('(Also ensure MySQL, Redis, Cassandra, RabbitMQ are running, e.g. docker-compose -f docker-compose/dependencies.yml up -d)\n');
    throw e;
  }
}

export default globalSetup;
