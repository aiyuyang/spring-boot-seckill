import request from '../utils/request';

// Login endpoint
export const login = (userId) => {
  return request.post('/auth/login', null, {
    params: { userId }
  });
};
