// Token management utility
export const setToken = (token) => {
  localStorage.setItem('token', token);
};

export const getToken = () => {
  return localStorage.getItem('token');
};

export const removeToken = () => {
  localStorage.removeItem('token');
};

export const isAuthenticated = () => {
  return !!getToken();
};

/**
 * Parse user ID from JWT token
 * @returns {number|null} User ID, returns null if parsing fails
 */
export const getUserIdFromToken = () => {
  try {
    const token = getToken();
    if (!token) {
      return null;
    }
    
    // JWT format: header.payload.signature
    // We need to parse the payload part
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }
    
    // Decode payload (base64)
    const payload = parts[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    
    // Get userId from payload
    return decoded.userId || null;
  } catch (error) {
    console.error('Token parsing failed:', error);
    return null;
  }
};
