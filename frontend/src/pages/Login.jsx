import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import { setToken } from '../utils/auth';

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const res = await login(values.userId);
      if (res.code === 200) {
        setToken(res.obj.token);
        message.success('登录成功');
        navigate('/');
      }
    } catch (error) {
      message.error('登录失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <Card 
        title="秒杀系统登录" 
        style={{ width: 400 }}
        styles={{ header: { textAlign: 'center', fontSize: '24px', fontWeight: 'bold' } }}
      >
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="userId"
            rules={[
              { required: true, message: '请输入用户ID' },
              { pattern: /^\d+$/, message: '用户ID必须是数字' }
            ]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="请输入用户ID（例如：1001）" 
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              block 
              loading={loading}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center', color: '#999', marginTop: '16px' }}>
          <p>提示：输入任意数字ID即可登录（模拟登录）</p>
        </div>
      </Card>
    </div>
  );
};

export default Login;
