import axios from 'axios';
import { message } from 'antd';

// Create axios instance
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  timeout: 10000,
});

// Request interceptor - Add token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Unified error handling
request.interceptors.response.use(
  (response) => {
    // If it's blob type (e.g., captcha image), return blob directly
    if (response.config.responseType === 'blob') {
      return response.data;
    }
    
    const res = response.data;
    // Backend response format: { code, message, obj }
    if (res.code === 200) {
      return res;
    } else {
      // Non-200 status code, show error message and reject response object
      // This allows business layer to get details via error.code and error.message
      message.error(res.message || '请求失败');
      return Promise.reject(res);
    }
  },
  (error) => {
    // Check if it's a business error (response object rejected from response interceptor)
    if (error.code && error.message) {
      // This is a business error, already handled in response interceptor, reject directly
      return Promise.reject(error);
    }
    
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        // Token expired, clear and redirect to login
        localStorage.removeItem('token');
        window.location.href = '/login';
        message.error('登录已过期，请重新登录');
      } else {
        // If blob response error, try to parse error message
        if (error.config?.responseType === 'blob' && data instanceof Blob) {
          // Blob errors usually need to be converted to text for reading
          data.text().then(text => {
            try {
              const json = JSON.parse(text);
              message.error(json.message || '请求失败');
            } catch {
              message.error('请求失败');
            }
          });
        } else {
          // HTTP error, show error message
          message.error(data?.message || '请求失败');
        }
      }
    } else if (error.message) {
      // Network error or other errors
      message.error(error.message || '网络错误，请检查网络连接');
    } else {
      message.error('网络错误，请检查网络连接');
    }
    return Promise.reject(error);
  }
);

export default request;
