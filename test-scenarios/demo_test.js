import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
    vus: 5,
    duration: '1000s'
}

export default function () {
  const data = JSON.stringify({
      numberOfMillis: 100
  });

const params = {
  headers: {
    'Content-Type': 'application/json',
  },
};

let res = http.post(
  'http://task-service.local/api/task-service/execute-task',
  data,
  params
);

  check(res, { 'success scaling request': (r) => r.status === 200 })

  sleep(2)
}
