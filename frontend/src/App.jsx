import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, Layout } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import Login from './pages/Login';
import ActivityList from './pages/ActivityList';
import ActivityDetail from './pages/ActivityDetail';
import PrivateRoute from './components/PrivateRoute';
import NavBar from './components/NavBar';
import './App.css';

const { Content } = Layout;

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Layout style={{ minHeight: '100vh' }}>
                  <NavBar />
                  <Content>
                    <ActivityList />
                  </Content>
                </Layout>
              </PrivateRoute>
            }
          />
          <Route
            path="/activity/:id"
            element={
              <PrivateRoute>
                <Layout style={{ minHeight: '100vh' }}>
                  <NavBar />
                  <Content>
                    <ActivityDetail />
                  </Content>
                </Layout>
              </PrivateRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
