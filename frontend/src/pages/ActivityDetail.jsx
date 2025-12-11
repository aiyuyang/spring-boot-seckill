import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Card, 
  Typography, 
  Button, 
  Space, 
  Tag, 
  Input, 
  Image, 
  Spin, 
  message,
  Modal,
  Statistic,
  Row,
  Col
} from 'antd';
import { 
  ShoppingCartOutlined, 
  ReloadOutlined,
  ArrowLeftOutlined 
} from '@ant-design/icons';
import { getActivityDetail, getServerTime } from '../api/activity';
import { 
  getCaptcha, 
  getSeckillPath, 
  doSeckill, 
  getOrderResult 
} from '../api/seckill';
import { formatCountdown, getCountdown, setServerTimeOffset } from '../utils/timeSync';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;

const ActivityDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [activity, setActivity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [countdown, setCountdown] = useState(0);
  const [canSeckill, setCanSeckill] = useState(false);
  const [activityStarted, setActivityStarted] = useState(false);
  const [captchaUrl, setCaptchaUrl] = useState('');
  const [verifyCode, setVerifyCode] = useState('');
  const [seckilling, setSeckilling] = useState(false);
  const [polling, setPolling] = useState(false);
  const [orderResult, setOrderResult] = useState(null);
  const countdownTimer = useRef(null);
  const pollTimer = useRef(null);
  const hasLoadedRef = useRef(false);
  const currentIdRef = useRef(null);

  useEffect(() => {
    // If ID changes, reset load flag
    if (currentIdRef.current !== id) {
      hasLoadedRef.current = false;
      currentIdRef.current = id;
    }
    
    // Prevent duplicate requests
    if (hasLoadedRef.current) {
      return;
    }
    hasLoadedRef.current = true;
    
    loadActivity();
    return () => {
      if (countdownTimer.current) {
        clearInterval(countdownTimer.current);
      }
      if (pollTimer.current) {
        clearInterval(pollTimer.current);
      }
      // Clean up captcha URL to avoid memory leak
      if (captchaUrl) {
        URL.revokeObjectURL(captchaUrl);
      }
    };
  }, [id]);

  useEffect(() => {
    if (activity) {
      // Immediately calculate countdown once
      const remaining = getCountdown(activity.startTime);
      setCountdown(remaining);
      setActivityStarted(remaining === 0);
      setCanSeckill(remaining === 0 && activity.availableStock > 0);
      
      // Start countdown
      startCountdown();
    }
    return () => {
      if (countdownTimer.current) {
        clearInterval(countdownTimer.current);
      }
    };
  }, [activity]);

  // Auto-load captcha when seckill is available
  useEffect(() => {
    if (canSeckill && !polling && !captchaUrl && id) {
      loadCaptcha();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [canSeckill, polling, id]);

  const loadActivity = async () => {
    try {
      // First get server time for synchronization
      try {
        const timeRes = await getServerTime();
        if (timeRes.code === 200 && timeRes.obj) {
          // Prefer timestamp, if not available use serverTime string
          if (timeRes.obj.timestamp) {
            setServerTimeOffset(timeRes.obj.timestamp);
          } else if (timeRes.obj.serverTime) {
            setServerTimeOffset(timeRes.obj.serverTime);
          }
        }
      } catch (timeError) {
        console.warn('获取服务器时间失败，使用客户端时间:', timeError);
        // If getting server time fails, reset offset to 0 (use client time)
        setServerTimeOffset(Date.now());
      }

      // Get activity details
      const res = await getActivityDetail(id);
      if (res.code === 200) {
        const activity = res.obj;
        if (activity) {
          setActivity(activity);
          // Immediately initialize countdown
          const remaining = getCountdown(activity.startTime);
          setCountdown(remaining);
          setActivityStarted(remaining === 0);
          setCanSeckill(remaining === 0 && activity.availableStock > 0);
        } else {
          message.error('活动不存在');
          navigate('/');
        }
      }
    } catch (error) {
      // Handle business errors
      if (error.code && error.message) {
        message.error(error.message);
      } else {
        message.error('加载活动详情失败');
      }
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const startCountdown = () => {
    if (countdownTimer.current) {
      clearInterval(countdownTimer.current);
    }

    countdownTimer.current = setInterval(() => {
      const remaining = getCountdown(activity.startTime);
      setCountdown(remaining);
      setActivityStarted(remaining === 0);
      setCanSeckill(remaining === 0 && activity.availableStock > 0);
      
      if (remaining === 0) {
        clearInterval(countdownTimer.current);
      }
    }, 1000);
  };

  const loadCaptcha = async () => {
    try {
      // If captcha URL already exists, release it first to avoid memory leak
      if (captchaUrl) {
        URL.revokeObjectURL(captchaUrl);
      }
      
      // Clear captcha input field
      setVerifyCode('');
      
      const blob = await getCaptcha(id);
      
      // Verify if response is a Blob object
      if (!(blob instanceof Blob)) {
        console.error('验证码响应不是 Blob 类型:', blob);
        message.error('验证码格式错误');
        return;
      }
      
      const url = URL.createObjectURL(blob);
      setCaptchaUrl(url);
    } catch (error) {
      console.error('加载验证码失败:', error);
      message.error('加载验证码失败: ' + (error.message || '未知错误'));
    }
  };

  const handleSeckill = async () => {
    if (!verifyCode) {
      message.warning('请输入验证码');
      return;
    }

    setSeckilling(true);
    try {
      // 1. Get seckill token (verify captcha + get token)
      const pathRes = await getSeckillPath(id, verifyCode);
      if (pathRes.code === 200) {
        const token = pathRes.obj;
        
        // 2. Execute seckill
        const seckillRes = await doSeckill(id, token);
        if (seckillRes.code === 200) {
          message.success('秒杀请求已提交，正在排队中...');
          setPolling(true);
          startPolling();
        }
      }
    } catch (error) {
      // Handle business errors (RespBean format from backend)
      // Note: If error has code and message, it's a business error, error message already shown in response interceptor
      // Here only need to handle business logic (e.g., refresh captcha), no need to show error message again
      if (!error.code || !error.message) {
        // Only non-business errors (e.g., network errors) are shown here
        message.error(error.message || '秒杀失败');
      }
      loadCaptcha(); // Refresh captcha
    } finally {
      setSeckilling(false);
    }
  };

  const startPolling = () => {
    if (pollTimer.current) {
      clearInterval(pollTimer.current);
    }

    pollTimer.current = setInterval(async () => {
      try {
        const res = await getOrderResult(id);
        if (res.code === 200) {
          const result = res.obj;
          if (result.status === 'success') {
            setOrderResult(result);
            setPolling(false);
            clearInterval(pollTimer.current);
            Modal.success({
              title: '秒杀成功！',
              content: (
                <div>
                  <p>订单号：{result.orderId}</p>
                  <p>订单金额：¥{result.orderPrice}</p>
                  <p>创建时间：{dayjs(result.createTime).format('YYYY-MM-DD HH:mm:ss')}</p>
                </div>
              ),
              onOk: () => navigate('/'),
            });
          } else if (result.status === 'waiting') {
            // Continue polling
          }
        }
      } catch (error) {
        // Polling failed, continue trying
      }
    }, 1000); // Poll every second
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!activity) {
    return null;
  }

  const discount = ((1 - activity.seckillPrice / activity.originalPrice) * 100).toFixed(0);

  return (
    <div style={{ 
      padding: '24px', 
      maxWidth: '1200px', 
      margin: '0 auto',
      background: 'linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%)',
      minHeight: 'calc(100vh - 64px)'
    }}>
      <Button 
        icon={<ArrowLeftOutlined />} 
        onClick={() => navigate('/')}
        style={{ marginBottom: '24px' }}
        size="large"
      >
        返回列表
      </Button>

      <Card
        style={{
          boxShadow: '0 4px 16px rgba(0,0,0,0.1)',
          borderRadius: '12px',
          border: 'none'
        }}
      >
        <Row gutter={24}>
          <Col xs={24} md={12}>
            <div style={{ textAlign: 'center', marginBottom: '32px' }}>
              <div style={{ 
                fontSize: '48px', 
                lineHeight: '1.2',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                fontWeight: 'bold',
                marginBottom: '16px'
              }}>
                {activity.name}
              </div>
            </div>

            <Space orientation="vertical" size="large" style={{ width: '100%' }}>
              <div style={{
                background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
                padding: '24px',
                borderRadius: '12px',
                textAlign: 'center'
              }}>
                <Text type="secondary" delete style={{ fontSize: '20px', display: 'block', marginBottom: '8px' }}>
                  原价：¥{activity.originalPrice}
                </Text>
                <Title level={1} type="danger" style={{ margin: '8px 0', fontSize: '36px' }}>
                  ¥{activity.seckillPrice}
                </Title>
                <Tag color="red" style={{ fontSize: '18px', padding: '6px 16px', borderRadius: '20px' }}>
                  💥 {discount}% OFF
                </Tag>
              </div>

              <Row gutter={16}>
                <Col span={12}>
                  <Statistic 
                    title="库存" 
                    value={activity.availableStock} 
                    suffix={`/ ${activity.initialStock}`}
                  />
                </Col>
                <Col span={12}>
                  <Statistic 
                    title="剩余库存率" 
                    value={((activity.availableStock / activity.initialStock) * 100).toFixed(1)} 
                    suffix="%"
                  />
                </Col>
              </Row>

              <div>
                <Text type="secondary">开始时间：</Text>
                <Text>{dayjs(activity.startTime).format('YYYY-MM-DD HH:mm:ss')}</Text>
              </div>
              <div>
                <Text type="secondary">结束时间：</Text>
                <Text>{dayjs(activity.endTime).format('YYYY-MM-DD HH:mm:ss')}</Text>
              </div>
            </Space>
          </Col>

          <Col xs={24} md={12}>
            <Card 
              title={
                <Space>
                  <span>⏰ 秒杀倒计时</span>
                  {polling && <Tag color="processing">排队中...</Tag>}
                </Space>
              }
              style={{ 
                marginBottom: '24px',
                background: countdown > 0 
                  ? 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)'
                  : 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)',
                border: 'none',
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
              }}
            >
              <div style={{ textAlign: 'center', padding: '30px 20px' }}>
                {countdown > 0 ? (
                  <>
                    <div style={{ 
                      fontSize: '48px', 
                      fontWeight: 'bold',
                      color: '#ff4d4f',
                      fontFamily: 'monospace',
                      letterSpacing: '4px',
                      marginBottom: '16px',
                      textShadow: '2px 2px 4px rgba(0,0,0,0.1)'
                    }}>
                      {formatCountdown(countdown)}
                    </div>
                    <Text type="secondary" style={{ fontSize: '16px' }}>
                      ⏳ 距离开始还有
                    </Text>
                  </>
                ) : activityStarted ? (
                  <>
                    <div style={{ 
                      fontSize: '36px', 
                      fontWeight: 'bold',
                      color: '#52c41a',
                      marginBottom: '16px'
                    }}>
                      🎉 活动已开始
                    </div>
                    {canSeckill ? (
                      <Text type="success" style={{ fontSize: '18px', fontWeight: 'bold' }}>
                        ✅ 可以开始秒杀！
                      </Text>
                    ) : activity.availableStock === 0 ? (
                      <Text type="danger" style={{ fontSize: '16px' }}>
                        ❌ 商品已售罄
                      </Text>
                    ) : (
                      <Text type="warning" style={{ fontSize: '16px' }}>
                        ⚠️ 活动进行中
                      </Text>
                    )}
                  </>
                ) : (
                  <>
                    <div style={{ 
                      fontSize: '48px', 
                      fontWeight: 'bold',
                      color: '#52c41a',
                      fontFamily: 'monospace',
                      letterSpacing: '4px',
                      marginBottom: '16px'
                    }}>
                      {formatCountdown(countdown)}
                    </div>
                    <Text type="secondary" style={{ fontSize: '16px' }}>
                      ⏰ 倒计时
                    </Text>
                  </>
                )}
              </div>
            </Card>

            {canSeckill && !polling && (
              <Card 
                title="🔐 验证码" 
                style={{ 
                  marginBottom: '24px',
                  background: '#fafafa',
                  borderRadius: '8px'
                }}
              >
                <Space orientation="vertical" style={{ width: '100%' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {captchaUrl && (
                      <Image 
                        src={captchaUrl} 
                        alt="验证码" 
                        width={130}
                        height={32}
                        preview={false}
                      />
                    )}
                    <Button 
                      icon={<ReloadOutlined />} 
                      onClick={loadCaptcha}
                      size="small"
                    >
                      刷新
                    </Button>
                  </div>
                  <Input
                    placeholder="请输入验证码"
                    value={verifyCode}
                    onChange={(e) => setVerifyCode(e.target.value)}
                    onPressEnter={handleSeckill}
                  />
                </Space>
              </Card>
            )}

            <Button
              type="primary"
              size="large"
              block
              icon={<ShoppingCartOutlined />}
              loading={seckilling || polling}
              disabled={!canSeckill || polling}
              onClick={handleSeckill}
              style={{ 
                height: '56px', 
                fontSize: '20px',
                fontWeight: 'bold',
                background: canSeckill && !polling 
                  ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                  : undefined,
                border: 'none',
                borderRadius: '8px',
                boxShadow: canSeckill && !polling ? '0 4px 12px rgba(102, 126, 234, 0.4)' : undefined
              }}
            >
              {polling ? '⏳ 排队中，请稍候...' : countdown > 0 ? '⏰ 等待开始' : '🚀 立即秒杀'}
            </Button>

            {!captchaUrl && canSeckill && (
              <Button
                block
                onClick={loadCaptcha}
                style={{ marginTop: '16px' }}
              >
                加载验证码
              </Button>
            )}
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default ActivityDetail;
