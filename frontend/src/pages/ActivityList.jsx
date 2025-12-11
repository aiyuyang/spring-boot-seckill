import { useEffect, useState, useRef } from 'react';
import { Card, List, Typography, Tag, Button, Space, Spin, message } from 'antd';
import { ShoppingCartOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getActivityList } from '../api/activity';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const ActivityList = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const hasLoadedRef = useRef(false);

  useEffect(() => {
    // Prevent duplicate requests
    if (hasLoadedRef.current) {
      return;
    }
    hasLoadedRef.current = true;
    
    loadActivities();
  }, []);

  const loadActivities = async () => {
    try {
      const res = await getActivityList();
      if (res.code === 200) {
        setActivities(res.obj || []);
      }
    } catch (error) {
      // Handle business errors
      if (error.code && error.message) {
        message.error(error.message);
      } else {
        message.error('加载活动列表失败');
      }
    } finally {
      setLoading(false);
    }
  };

  const getStatus = (activity) => {
    const now = dayjs();
    const start = dayjs(activity.startTime);
    const end = dayjs(activity.endTime);

    if (now.isBefore(start)) {
      return { text: '未开始', color: 'default' };
    } else if (now.isAfter(end) || activity.availableStock === 0) {
      return { text: '已结束', color: 'error' };
    } else {
      return { text: '进行中', color: 'success' };
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ 
      padding: '24px', 
      maxWidth: '1200px', 
      margin: '0 auto',
      background: 'linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%)',
      minHeight: 'calc(100vh - 64px)'
    }}>
      <Title level={2} style={{ 
        textAlign: 'center', 
        marginBottom: '32px',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        WebkitBackgroundClip: 'text',
        WebkitTextFillColor: 'transparent',
        fontSize: '32px',
        fontWeight: 'bold'
      }}>
        🛒 秒杀活动列表
      </Title>

      <List
        grid={{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 3 }}
        dataSource={activities}
        renderItem={(activity) => {
          const status = getStatus(activity);
          return (
            <List.Item>
              <Card
                hoverable
                style={{ 
                  height: '100%',
                  borderRadius: '12px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                  transition: 'all 0.3s',
                  border: 'none'
                }}
                bodyStyle={{ padding: '20px' }}
                actions={[
                  <Button
                    type="primary"
                    icon={<ShoppingCartOutlined />}
                    onClick={() => navigate(`/activity/${activity.id}`)}
                    disabled={status.text !== '进行中'}
                    block
                    style={{
                      background: status.text === '进行中' 
                        ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                        : undefined,
                      border: 'none',
                      borderRadius: '0 0 12px 12px',
                      height: '48px',
                      fontSize: '16px',
                      fontWeight: 'bold'
                    }}
                  >
                    {status.text === '进行中' ? '🚀 立即秒杀' : status.text}
                  </Button>
                ]}
              >
                <Card.Meta
                  title={
                    <Space>
                      <span>{activity.name}</span>
                      <Tag color={status.color}>{status.text}</Tag>
                    </Space>
                  }
                  description={
                    <div>
                      <div style={{ 
                        marginBottom: '12px',
                        padding: '12px',
                        background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
                        borderRadius: '8px'
                      }}>
                        <div style={{ marginBottom: '4px' }}>
                          <Text type="secondary" style={{ fontSize: '14px' }}>原价：</Text>
                          <Text delete style={{ fontSize: '16px' }}>¥{activity.originalPrice}</Text>
                        </div>
                        <div>
                          <Text type="danger" strong style={{ fontSize: '24px', fontWeight: 'bold' }}>
                            💥 秒杀价：¥{activity.seckillPrice}
                          </Text>
                        </div>
                      </div>
                      <div style={{ marginBottom: '8px', padding: '8px', background: '#f0f0f0', borderRadius: '6px' }}>
                        <Text type="secondary" style={{ fontSize: '14px' }}>📦 库存：</Text>
                        <Text strong style={{ fontSize: '16px' }}>
                          {activity.availableStock} / {activity.initialStock}
                        </Text>
                        <div style={{ 
                          width: '100%', 
                          height: '8px', 
                          background: '#e0e0e0', 
                          borderRadius: '4px',
                          marginTop: '4px',
                          overflow: 'hidden'
                        }}>
                          <div style={{
                            width: `${(activity.availableStock / activity.initialStock) * 100}%`,
                            height: '100%',
                            background: activity.availableStock > activity.initialStock * 0.3 
                              ? 'linear-gradient(90deg, #52c41a, #73d13d)'
                              : 'linear-gradient(90deg, #ff4d4f, #ff7875)',
                            transition: 'width 0.3s'
                          }} />
                        </div>
                      </div>
                      <div style={{ marginTop: '12px', padding: '8px', background: '#f9f9f9', borderRadius: '6px' }}>
                        <div style={{ marginBottom: '4px' }}>
                          <ClockCircleOutlined style={{ marginRight: '4px', color: '#1890ff' }} />
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            开始：{dayjs(activity.startTime).format('YYYY-MM-DD HH:mm:ss')}
                          </Text>
                        </div>
                        <div>
                          <ClockCircleOutlined style={{ marginRight: '4px', color: '#ff4d4f' }} />
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            结束：{dayjs(activity.endTime).format('YYYY-MM-DD HH:mm:ss')}
                          </Text>
                        </div>
                      </div>
                    </div>
                  }
                />
              </Card>
            </List.Item>
          );
        }}
      />
    </div>
  );
};

export default ActivityList;
