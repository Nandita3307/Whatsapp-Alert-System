// src/pages/Analytics.jsx
// ─────────────────────────────────────────────────────────────────────────────
// Advanced Dynamic Analytics Engine
//   • User writes any SQL query
//   • Backend analyses column types and suggests the best chart
//   • Supports: Bar, Line, Pie, Doughnut, Area, Scatter
//   • Manual chart type override
//   • Download chart as PNG
//   • Download analytics as PDF
//   • Send analytics PDF via WhatsApp
// ─────────────────────────────────────────────────────────────────────────────
import { useState, useRef, useCallback } from 'react';
import {
  Row, Col, Card, Button, Input, Select, Typography, Space, Tag,
  Alert, Table, Divider, Form, Modal, Spin, Empty, Tooltip, Badge
} from 'antd';
import {
  PlayCircleOutlined, BarChartOutlined, LineChartOutlined,
  PieChartOutlined, DownloadOutlined, SendOutlined, WhatsAppOutlined,
  ThunderboltOutlined, FilePdfOutlined, ReloadOutlined, ClockCircleOutlined
} from '@ant-design/icons';
import {
  Chart as ChartJS,
  CategoryScale, LinearScale, BarElement,
  PointElement, LineElement, ArcElement,
  Title as ChartTitle, Tooltip as ChartTooltip, Legend, Filler
} from 'chart.js';
import { Bar, Line, Pie, Doughnut, Scatter } from 'react-chartjs-2';
import toast from 'react-hot-toast';
import api from '../api/axios';

ChartJS.register(
  CategoryScale, LinearScale, BarElement,
  PointElement, LineElement, ArcElement,
  ChartTitle, ChartTooltip, Legend, Filler
);

const { TextArea } = Input;
const { Title, Text } = Typography;
const { Option } = Select;

// ── Colour palette ────────────────────────────────────────────────────────────
const PALETTE = [
  'rgba(37,211,102,0.8)',  'rgba(22,119,255,0.8)',  'rgba(250,140,22,0.8)',
  'rgba(114,46,209,0.8)',  'rgba(235,47,150,0.8)',  'rgba(19,194,194,0.8)',
  'rgba(82,196,26,0.8)',   'rgba(255,77,79,0.8)',   'rgba(250,173,20,0.8)',
  'rgba(47,84,235,0.8)',
];
const PALETTE_BORDER = PALETTE.map(c => c.replace('0.8', '1'));

// ── Sample queries ────────────────────────────────────────────────────────────
const SAMPLE_QUERIES = [
  {
    label: '👥 Employees by Department',
    sql: 'SELECT department, COUNT(*) AS total FROM employee_details GROUP BY department ORDER BY total DESC',
  },
  {
    label: '💰 Salary by Month (trend)',
    sql: "SELECT month, AVG(amount) AS avg_salary FROM salary_details WHERE component='total' GROUP BY month",
  },
  {
    label: '💼 Salary by Employee',
    sql: "SELECT e.name, MAX(CASE WHEN s.component='total' THEN s.amount END) AS salary FROM employee_details e JOIN salary_details s ON e.s_no=s.det_s_no GROUP BY e.s_no, e.name",
  },
  {
    label: '📊 Component Breakdown',
    sql: "SELECT component, SUM(amount) AS total FROM salary_details GROUP BY component",
  },
];

// ── Chart type options ────────────────────────────────────────────────────────
const CHART_TYPES = [
  { label: '🤖 Auto Detect',   value: 'auto' },
  { label: '📊 Bar Chart',     value: 'bar' },
  { label: '📈 Line Chart',    value: 'line' },
  { label: '🥧 Pie Chart',     value: 'pie' },
  { label: '🍩 Doughnut',      value: 'doughnut' },
  { label: '🌊 Area Chart',    value: 'area' },
  { label: '⚡ Scatter Plot',  value: 'scatter' },
];

// ── Build Chart.js dataset from analytics result ──────────────────────────────
function buildChartData(columns, rows, xAxis, yAxis, chartType) {
  if (!columns?.length || !rows?.length) return null;

  const xIdx = xAxis ? columns.indexOf(xAxis) : 0;
  const yIdx = yAxis ? columns.indexOf(yAxis) : 1;

  if (chartType === 'scatter') {
    const points = rows.map(row => ({
      x: Number(row[xIdx]) || 0,
      y: Number(row[yIdx]) || 0,
    }));
    return {
      datasets: [{
        label: `${columns[xIdx]} vs ${columns[yIdx]}`,
        data: points,
        backgroundColor: PALETTE[0],
        pointRadius: 6,
      }],
    };
  }

  const labels = rows.map(row => row[xIdx] != null ? String(row[xIdx]) : '');
  const values = rows.map(row => Number(row[yIdx]) || 0);

  const isArc = ['pie', 'doughnut'].includes(chartType);
  const bg    = isArc
    ? rows.map((_, i) => PALETTE[i % PALETTE.length])
    : PALETTE[0];
  const border = isArc
    ? rows.map((_, i) => PALETTE_BORDER[i % PALETTE_BORDER.length])
    : PALETTE_BORDER[0];

  return {
    labels,
    datasets: [{
      label: columns[yIdx] || 'Value',
      data: values,
      backgroundColor: bg,
      borderColor: border,
      borderWidth: 2,
      fill: chartType === 'area',
      tension: 0.4,
      pointRadius: chartType === 'line' || chartType === 'area' ? 5 : undefined,
    }],
  };
}

// ── Chart renderer ────────────────────────────────────────────────────────────
function ChartRenderer({ type, data, chartRef }) {
  const opts = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { font: { size: 12 }, padding: 16 } },
    },
  };
  const axisOpts = {
    ...opts,
    scales: {
      x: { grid: { display: false } },
      y: { beginAtZero: true, ticks: { maxTicksLimit: 6 } },
    },
  };

  if (!data) return <Empty description="No data to display" />;

  const props = { ref: chartRef, data, options: type === 'scatter' ? axisOpts : opts };

  switch (type) {
    case 'line':     return <div style={{ height: 340 }}><Line {...props} options={axisOpts} /></div>;
    case 'area':     return <div style={{ height: 340 }}><Line {...props} options={axisOpts} /></div>;
    case 'pie':      return <div style={{ height: 340 }}><Pie {...props} /></div>;
    case 'doughnut': return <div style={{ height: 340 }}><Doughnut {...props} /></div>;
    case 'scatter':  return <div style={{ height: 340 }}><Scatter {...props} options={axisOpts} /></div>;
    default:         return <div style={{ height: 340 }}><Bar {...props} options={axisOpts} /></div>;
  }
}

// ─────────────────────────────────────────────────────────────────────────────

export default function Analytics() {
  const [sql, setSql]               = useState(SAMPLE_QUERIES[0].sql);
  const [queryLoading, setQueryLoading] = useState(false);
  const [execMs, setExecMs]         = useState(null);

  // Raw query result
  const [columns, setColumns]       = useState([]);
  const [rows, setRows]             = useState([]);
  const [totalRows, setTotalRows]   = useState(0);
  const [columnTypes, setColumnTypes] = useState([]);

  // Chart config
  const [chartTypeSelect, setChartTypeSelect] = useState('auto');
  const [activeType, setActiveType] = useState(null);   // resolved type after auto
  const [xAxis, setXAxis]           = useState(null);
  const [yAxis, setYAxis]           = useState(null);
  const [chartData, setChartData]   = useState(null);

  // WhatsApp modal
  const [waModal, setWaModal]       = useState(false);
  const [waLoading, setWaLoading]   = useState(false);
  const [waForm]                    = Form.useForm();

  // Downloading flags
  const [pdfLoading, setPdfLoading] = useState(false);

  const chartRef = useRef(null);

  // ── Execute query ───────────────────────────────────────────────────────────
  const executeQuery = useCallback(async () => {
    if (!sql.trim()) return toast.error('Enter a SQL query');
    setQueryLoading(true);
    setChartData(null);
    setActiveType(null);

    try {
      const res = await api.post('/api/analytics/generate', { sql });
      const d   = res.data.data;

      setColumns(d.columns || []);
      setRows(d.rows || []);
      setTotalRows(d.totalRows || 0);
      setColumnTypes(d.columnTypes || []);
      setExecMs(d.executionMs);

      const suggestion = d.chartSuggestion || {};
      const resolvedType = chartTypeSelect === 'auto'
            ? (suggestion.type || 'bar')
            : chartTypeSelect;

      const rx = suggestion.xAxis || d.columns?.[0] || '';
      const ry = suggestion.yAxis || d.columns?.[1] || '';

      setXAxis(rx);
      setYAxis(ry);
      setActiveType(resolvedType);
      setChartData(buildChartData(d.columns, d.rows, rx, ry, resolvedType));

      toast.success(
        `${d.totalRows} rows loaded. Chart: ${resolvedType}` +
        (chartTypeSelect === 'auto' ? ' (auto-detected)' : '')
      );
    } catch (err) {
      toast.error(err.response?.data?.message || 'Query failed');
    } finally {
      setQueryLoading(false);
    }
  }, [sql, chartTypeSelect]);

  // Re-render chart when user manually changes type
  const changeChartType = (type) => {
    setChartTypeSelect(type);
    if (!columns.length) return;
    const resolved = type === 'auto' ? (activeType || 'bar') : type;
    setActiveType(resolved);
    setChartData(buildChartData(columns, rows, xAxis, yAxis, resolved));
  };

  // Re-render when axis columns change
  const changeAxis = (axis, value) => {
    if (axis === 'x') { setXAxis(value); setChartData(buildChartData(columns, rows, value, yAxis, activeType)); }
    else              { setYAxis(value); setChartData(buildChartData(columns, rows, xAxis, value, activeType)); }
  };

  // ── Download chart as PNG ───────────────────────────────────────────────────
  const downloadChartPng = () => {
    const instance = chartRef.current;
    if (!instance) { toast.error('Chart not rendered yet'); return; }
    const url = instance.toBase64Image();
    const a   = document.createElement('a');
    a.href     = url;
    a.download = 'analytics_chart.png';
    a.click();
    toast.success('Chart downloaded as PNG');
  };

  // ── Download analytics as PDF ───────────────────────────────────────────────
  const downloadPdf = async () => {
    if (!sql.trim()) return toast.error('Run a query first');
    setPdfLoading(true);
    try {
      const res = await api.post('/api/analytics/download-pdf',
        { sql, title: 'Analytics Report' },
        { responseType: 'blob' }
      );
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const a   = document.createElement('a');
      a.href = url;
      a.download = 'analytics_report.pdf';
      a.click();
      toast.success('Analytics PDF downloaded');
    } catch {
      toast.error('PDF download failed');
    } finally {
      setPdfLoading(false);
    }
  };

  // ── Send via WhatsApp ───────────────────────────────────────────────────────
  const handleSendWhatsApp = async (values) => {
    setWaLoading(true);
    try {
      const res = await api.post('/api/analytics/send-whatsapp', {
        phoneNumber: values.phoneNumber,
        message:     values.message || 'Analytics report attached.',
        sql,
        title:       'Analytics Report',
      });
      if (res.data.success) {
        toast.success('Analytics PDF sent via WhatsApp!');
        setWaModal(false);
        waForm.resetFields();
      } else {
        toast.error(res.data.message);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'WhatsApp send failed');
    } finally {
      setWaLoading(false);
    }
  };

  // ── Table columns for raw data tab ─────────────────────────────────────────
  const tableColumns = columns.map((col, i) => ({
    title: (
      <span>
        {col}
        <Tag
          style={{ marginLeft: 4, fontSize: 9, padding: '0 3px' }}
          color={columnTypes[i] === 'NUMERIC' ? 'blue' : columnTypes[i] === 'DATE' ? 'green' : 'default'}
        >
          {columnTypes[i] || 'TEXT'}
        </Tag>
      </span>
    ),
    dataIndex: col, key: col, ellipsis: true, width: 130,
    render: v => v == null ? <Text type="secondary" style={{ fontSize: 11 }}>NULL</Text> : String(v),
  }));

  const tableData = rows.slice(0, 100).map((row, i) => {
    const obj = { key: i };
    columns.forEach((c, j) => { obj[c] = row[j]; });
    return obj;
  });

  // ─────────────────────────────────────────────────────────────────────────
  return (
    <div>
      {/* Page header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>
            <BarChartOutlined style={{ color: '#25D366', marginRight: 8 }} />
            Dynamic Analytics Engine
          </Title>
          <Text type="secondary">
            Write any SQL → auto-generate charts → download or share
          </Text>
        </div>
        {execMs != null && (
          <Tag icon={<ClockCircleOutlined />} color="blue">{execMs} ms</Tag>
        )}
      </div>

      <Row gutter={[16, 16]}>

        {/* ── LEFT: Query + Chart controls ── */}
        <Col xs={24} lg={8}>

          {/* SQL Editor */}
          <Card
            bordered={false}
            style={{ borderRadius: 12, marginBottom: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}
            title="🗄️ SQL Query"
            extra={
              <Select
                size="small"
                style={{ width: 180 }}
                placeholder="Load sample"
                onChange={val => setSql(val)}
                allowClear
              >
                {SAMPLE_QUERIES.map((q, i) => (
                  <Option key={i} value={q.sql}>{q.label}</Option>
                ))}
              </Select>
            }
          >
            <TextArea
              value={sql}
              onChange={e => setSql(e.target.value)}
              rows={8}
              style={{ fontFamily: 'monospace', fontSize: 12, marginBottom: 12 }}
              placeholder="SELECT department, COUNT(*) AS total FROM employees GROUP BY department"
            />
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              onClick={executeQuery}
              loading={queryLoading}
              block
              style={{ background: '#25D366', borderColor: '#25D366', fontWeight: 600 }}
            >
              {queryLoading ? 'Analysing...' : 'Execute & Generate Chart'}
            </Button>
            <Alert
              message="Only SELECT queries are permitted."
              type="info" showIcon style={{ marginTop: 10, borderRadius: 8 }}
            />
          </Card>

          {/* Chart controls */}
          {columns.length > 0 && (
            <Card
              bordered={false}
              style={{ borderRadius: 12, marginBottom: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}
              title="🎛️ Chart Controls"
            >
              <Space direction="vertical" style={{ width: '100%' }} size={10}>
                <div>
                  <Text strong style={{ fontSize: 12, display: 'block', marginBottom: 4 }}>Chart Type</Text>
                  <Select
                    value={chartTypeSelect}
                    onChange={changeChartType}
                    style={{ width: '100%' }}
                  >
                    {CHART_TYPES.map(t => (
                      <Option key={t.value} value={t.value}>{t.label}</Option>
                    ))}
                  </Select>
                  {chartTypeSelect === 'auto' && activeType && (
                    <Text type="secondary" style={{ fontSize: 11 }}>
                      Auto-selected: <b>{activeType}</b>
                    </Text>
                  )}
                </div>

                <Row gutter={8}>
                  <Col span={12}>
                    <Text strong style={{ fontSize: 12, display: 'block', marginBottom: 4 }}>X Axis</Text>
                    <Select value={xAxis} onChange={v => changeAxis('x', v)} style={{ width: '100%' }} size="small">
                      {columns.map(c => <Option key={c} value={c}>{c}</Option>)}
                    </Select>
                  </Col>
                  <Col span={12}>
                    <Text strong style={{ fontSize: 12, display: 'block', marginBottom: 4 }}>Y Axis</Text>
                    <Select value={yAxis} onChange={v => changeAxis('y', v)} style={{ width: '100%' }} size="small">
                      {columns.map(c => <Option key={c} value={c}>{c}</Option>)}
                    </Select>
                  </Col>
                </Row>
              </Space>
            </Card>
          )}

          {/* Export / Share */}
          {chartData && (
            <Card
              bordered={false}
              style={{ borderRadius: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}
              title="📥 Export & Share"
            >
              <Space direction="vertical" style={{ width: '100%' }} size={8}>
                <Button
                  icon={<DownloadOutlined />}
                  onClick={downloadChartPng}
                  block
                >
                  Download Chart (PNG)
                </Button>
                <Button
                  icon={<FilePdfOutlined />}
                  onClick={downloadPdf}
                  loading={pdfLoading}
                  danger
                  block
                >
                  Download Analytics PDF
                </Button>
                <Divider style={{ margin: '4px 0' }} />
                <Button
                  icon={<WhatsAppOutlined />}
                  onClick={() => { waForm.resetFields(); setWaModal(true); }}
                  block
                  style={{ background: '#25D366', borderColor: '#25D366', color: '#fff' }}
                >
                  Send via WhatsApp
                </Button>
              </Space>
            </Card>
          )}
        </Col>

        {/* ── RIGHT: Chart + Data table ── */}
        <Col xs={24} lg={16}>

          {/* Chart */}
          <Card
            bordered={false}
            style={{ borderRadius: 12, marginBottom: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}
            title={
              <Space>
                <span>
                  {activeType === 'line' || activeType === 'area' ? '📈' :
                   activeType === 'pie'  || activeType === 'doughnut' ? '🥧' :
                   activeType === 'scatter' ? '⚡' : '📊'}
                  {' '}Analytics Chart
                </span>
                {activeType && (
                  <Tag color="blue">{activeType.charAt(0).toUpperCase() + activeType.slice(1)}</Tag>
                )}
                {totalRows > 0 && <Tag color="green">{totalRows} records</Tag>}
              </Space>
            }
          >
            {queryLoading ? (
              <div style={{ textAlign: 'center', padding: 80 }}>
                <Spin size="large" tip="Analysing data..." />
              </div>
            ) : chartData ? (
              <ChartRenderer
                type={activeType}
                data={chartData}
                chartRef={chartRef}
              />
            ) : (
              <div style={{ textAlign: 'center', padding: 80, color: '#bbb' }}>
                <BarChartOutlined style={{ fontSize: 56, marginBottom: 12 }} />
                <br />
                <Text type="secondary">
                  Write a SQL query and click <b>Execute &amp; Generate Chart</b>
                </Text>
              </div>
            )}
          </Card>

          {/* Raw Data Table */}
          {columns.length > 0 && (
            <Card
              bordered={false}
              style={{ borderRadius: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}
              title={
                <Space>
                  <span>🗃️ Raw Data</span>
                  <Tag color="blue">{totalRows} rows</Tag>
                  {totalRows > 100 && <Tag color="orange">Showing first 100</Tag>}
                </Space>
              }
            >
              <Table
                size="small"
                columns={tableColumns}
                dataSource={tableData}
                scroll={{ x: 'max-content', y: 260 }}
                pagination={{ pageSize: 20, size: 'small', showSizeChanger: false }}
                bordered
              />
            </Card>
          )}
        </Col>
      </Row>

      {/* ── WhatsApp Modal ── */}
      <Modal
        title={<span><WhatsAppOutlined style={{ color: '#25D366' }} /> Send Analytics via WhatsApp</span>}
        open={waModal}
        onCancel={() => { setWaModal(false); waForm.resetFields(); }}
        footer={null}
        width={480}
      >
        <Alert
          type="info"
          showIcon
          message="An analytics PDF (query + data table) will be generated and sent as an attachment."
          style={{ marginBottom: 16, borderRadius: 8 }}
        />
        <Form form={waForm} layout="vertical" onFinish={handleSendWhatsApp}>
          <Form.Item
            name="phoneNumber"
            label="Recipient WhatsApp Number"
            rules={[
              { required: true, message: 'Enter recipient number' },
              { pattern: /^\+?[0-9]{10,15}$/, message: 'Enter a valid number' },
            ]}
          >
            <Input prefix="📱" placeholder="+919876543210" size="large" />
          </Form.Item>
          <Form.Item
            name="message"
            label="Message"
            initialValue="Please find the analytics report attached."
          >
            <TextArea rows={3} placeholder="Custom message..." />
          </Form.Item>
          <Form.Item style={{ textAlign: 'right', marginBottom: 0 }}>
            <Space>
              <Button onClick={() => setWaModal(false)}>Cancel</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={waLoading}
                icon={<SendOutlined />}
                style={{ background: '#25D366', borderColor: '#25D366' }}
              >
                Send Analytics
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}