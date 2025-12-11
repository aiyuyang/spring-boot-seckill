import { useState, useEffect } from 'react';
import { Layout, Button, Space, Typography } from 'antd';
import { LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { removeToken, getUserIdFromToken } from '../utils/auth';
import { message } from 'antd';

const { Header } = Layout;
const { Text } = Typography;

const NavBar = () => {
  const navigate = useNavigate();
  const [userId, setUserId] = useState(null);

  useEffect(() => {
    // Parse user ID from token
    const id = getUserIdFromToken();
    setUserId(id);
  }, []);

  const handleLogout = () => {
    removeToken();
    message.success('已退出登录');
    navigate('/login');
  };

  return (
    <Header
      style={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        padding: '0 24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
      }}
    >
      <div style={{ color: 'white', fontSize: '20px', fontWeight: 'bold' }}>
        🛒 秒杀系统
      </div>
      <Space>
        <Text style={{ color: 'white', fontSize: '16px' }}>
          <UserOutlined /> 用户 {userId || '...'}
        </Text>
        <Button
          type="text"
          icon={<LogoutOutlined />}
          onClick={handleLogout}
          style={{ color: 'white' }}
        >
          退出登录
        </Button>
      </Space>
    </Header>
  );
};

export default NavBar;

