import request from '../utils/request';

// Get captcha image
export const getCaptcha = (activityId) => {
  return request.get('/seckill/captcha', {
    params: { activityId },
    responseType: 'blob'
  });
};

// Get seckill token (verify captcha + get token)
export const getSeckillPath = (activityId, verifyCode) => {
  return request.get(`/seckill/token/${activityId}`, {
    params: { verifyCode }
  });
};

// Execute seckill
export const doSeckill = (activityId, token) => {
  return request.post(`/seckill/doSeckill/${activityId}/${token}`);
};

// Query order result
export const getOrderResult = (activityId) => {
  return request.get(`/order/result/${activityId}`);
};
