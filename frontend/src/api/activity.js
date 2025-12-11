import request from '../utils/request';

// Get activity list
export const getActivityList = () => {
  return request.get('/activity/list');
};

// Get activity details
export const getActivityDetail = (activityId) => {
  return request.get(`/activity/${activityId}`);
};

// Get server time (for time synchronization)
export const getServerTime = () => {
  return request.get('/activity/serverTime');
};
