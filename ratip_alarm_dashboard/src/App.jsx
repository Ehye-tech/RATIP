import React, { useState, useEffect } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { AlertCircle, Activity, TrendingUp, Clock, Search, Bell, Zap, Database, Server, CheckCircle } from 'lucide-react';

const RATIP = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [query, setQuery] = useState('');
  const [aiResponse, setAiResponse] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [backendStatus, setBackendStatus] = useState('checking');
  const [telemetryData, setTelemetryData] = useState([]);
  const [alarms, setAlarms] = useState([]);
  const [correlations, setCorrelations] = useState([]);
  
  const API_BASE_URL = 'http://localhost:8080/api/v1';

  useEffect(() => {
    checkBackendHealth();
    const healthCheckInterval = setInterval(checkBackendHealth, 30000);
    return () => clearInterval(healthCheckInterval);
  }, []);

  const checkBackendHealth = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/health`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setBackendStatus(data.status === 'UP' ? 'connected' : 'error');
      } else {
        setBackendStatus('error');
      }
    } catch (error) {
      console.error('Backend health check failed:', error);
      setBackendStatus('disconnected');
    }
  };

  useEffect(() => {
    const generateTelemetryData = () => {
      const now = Date.now();
      const newData = Array.from({ length: 20 }, (_, i) => ({
        time: new Date(now - (19 - i) * 60000).toLocaleTimeString(),
        apiLatency: Math.random() * 100 + 50,
        lambdaDuration: Math.random() * 500 + 200,
        dynamoRead: Math.random() * 1000 + 500,
        errors: Math.floor(Math.random() * 10)
      }));
      setTelemetryData(newData);
    };

    generateTelemetryData();
    const interval = setInterval(generateTelemetryData, 5000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const alarmTypes = [
      { service: 'api-gateway', metric: 'High Latency', severity: 'WARNING' },
      { service: 'lambda-processor', metric: 'Error Rate Spike', severity: 'CRITICAL' },
      { service: 'dynamodb-table', metric: 'Throttling', severity: 'WARNING' },
      { service: 'ecs-service', metric: 'CPU Threshold', severity: 'INFO' },
      { service: 'kinesis-stream', metric: 'Shard Saturation', severity: 'CRITICAL' }
    ];

    const generateAlarms = () => {
      const newAlarms = Array.from({ length: 5 }, (_, i) => {
        const alarm = alarmTypes[Math.floor(Math.random() * alarmTypes.length)];
        return {
          id: `alarm-${Date.now()}-${i}`,
          ...alarm,
          timestamp: new Date(Date.now() - Math.random() * 3600000).toISOString(),
          value: Math.random() * 100
        };
      }).sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      setAlarms(newAlarms);
    };

    generateAlarms();
    const interval = setInterval(generateAlarms, 10000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const correlationPatterns = [
      'Lambda cold starts correlated with API latency spikes',
      'DynamoDB throttling events preceding error rate increase',
      'ECS memory pressure during peak traffic hours',
      'Kinesis lag causing downstream Lambda timeouts'
    ];

    const newCorrelations = correlationPatterns.map((pattern, i) => ({
      id: i,
      pattern,
      confidence: (Math.random() * 30 + 70).toFixed(1),
      occurrences: Math.floor(Math.random() * 20 + 5),
      lastSeen: new Date(Date.now() - Math.random() * 7200000).toLocaleString()
    }));
    setCorrelations(newCorrelations);
  }, []);

  const handleAIQuery = async () => {
    if (!query.trim()) return;
    
    setIsProcessing(true);
    setAiResponse('');

    try {
      const response = await fetch(`${API_BASE_URL}/query`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          query: query
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setAiResponse(data.response);
      } else {
        const errorData = await response.json();
        setAiResponse(`Error: ${errorData.error || 'Failed to process query'}`);
      }
    } catch (error) {
      console.error('Query failed:', error);
      
      setAiResponse(`Backend Connection Error\n\nThe Java Spring Boot backend is not available. Please ensure:\n\n1. The backend service is running on ${API_BASE_URL}\n2. CORS is properly configured\n3. The health check endpoint is accessible\n\nError: ${error.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const getSeverityColor = (severity) => {
    const colors = {
      CRITICAL: 'bg-red-500',
      WARNING: 'bg-yellow-500',
      INFO: 'bg-blue-500'
    };
    return colors[severity] || 'bg-gray-500';
  };

  const getBackendStatusBadge = () => {
    const statusConfig = {
      connected: { color: 'bg-green-500', text: 'Connected', icon: CheckCircle },
      disconnected: { color: 'bg-red-500', text: 'Disconnected', icon: AlertCircle },
      checking: { color: 'bg-yellow-500', text: 'Checking...', icon: Clock },
      error: { color: 'bg-red-500', text: 'Error', icon: AlertCircle }
    };
    
    const config = statusConfig[backendStatus];
    const Icon = config.icon;
    
    return (
      <div className={`flex items-center gap-2 ${config.color} text-white px-3 py-1 rounded-full text-sm`}>
        <Icon className="w-4 h-4" />
        <span>Backend: {config.text}</span>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white p-6">
      <div className="max-w-7xl mx-auto mb-8">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-3">
            <Activity className="w-8 h-8 text-cyan-400" />
            <div>
              <h1 className="text-3xl font-bold">RATIP</h1>
              <p className="text-slate-400 text-sm">Real-Time Alarm & Telemetry Intelligence Platform</p>
            </div>
          </div>
          {getBackendStatusBadge()}
        </div>
        
        <div className="bg-slate-800/50 rounded-lg p-3 border border-slate-700 mt-4">
          <div className="flex items-center gap-2 text-sm">
            <Server className="w-4 h-4 text-cyan-400" />
            <span className="text-slate-400">Backend API:</span>
            <code className="text-cyan-400 bg-slate-900 px-2 py-1 rounded">{API_BASE_URL}</code>
            <button 
              onClick={checkBackendHealth}
              className="ml-auto bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded text-xs transition-colors"
            >
              Test Connection
            </button>
          </div>
        </div>
        
        <div className="grid grid-cols-4 gap-4 mt-6">
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="flex items-center gap-2 mb-2">
              <Server className="w-5 h-5 text-green-400" />
              <span className="text-slate-400 text-sm">Services</span>
            </div>
            <div className="text-2xl font-bold">47</div>
          </div>
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="flex items-center gap-2 mb-2">
              <AlertCircle className="w-5 h-5 text-yellow-400" />
              <span className="text-slate-400 text-sm">Active Alarms</span>
            </div>
            <div className="text-2xl font-bold">{alarms.length}</div>
          </div>
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp className="w-5 h-5 text-cyan-400" />
              <span className="text-slate-400 text-sm">Correlations</span>
            </div>
            <div className="text-2xl font-bold">{correlations.length}</div>
          </div>
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="flex items-center gap-2 mb-2">
              <Database className="w-5 h-5 text-purple-400" />
              <span className="text-slate-400 text-sm">Events/min</span>
            </div>
            <div className="text-2xl font-bold">12.4k</div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto mb-6">
        <div className="flex gap-2 bg-slate-800 p-1 rounded-lg inline-flex">
          {['dashboard', 'ai-query', 'alarms', 'correlations'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-6 py-2 rounded-md transition-all ${
                activeTab === tab
                  ? 'bg-cyan-600 text-white'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              {tab.split('-').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')}
            </button>
          ))}
        </div>
      </div>

      <div className="max-w-7xl mx-auto">
        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-6">
              <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
                <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                  <Zap className="w-5 h-5 text-yellow-400" />
                  API Latency & Lambda Duration
                </h3>
                <ResponsiveContainer width="100%" height={250}>
                  <LineChart data={telemetryData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                    <XAxis dataKey="time" stroke="#94a3b8" />
                    <YAxis stroke="#94a3b8" />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155' }}
                    />
                    <Legend />
                    <Line type="monotone" dataKey="apiLatency" stroke="#06b6d4" name="API Latency (ms)" />
                    <Line type="monotone" dataKey="lambdaDuration" stroke="#f59e0b" name="Lambda Duration (ms)" />
                  </LineChart>
                </ResponsiveContainer>
              </div>

              <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
                <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                  <Database className="w-5 h-5 text-purple-400" />
                  DynamoDB Operations & Errors
                </h3>
                <ResponsiveContainer width="100%" height={250}>
                  <BarChart data={telemetryData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                    <XAxis dataKey="time" stroke="#94a3b8" />
                    <YAxis stroke="#94a3b8" />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155' }}
                    />
                    <Legend />
                    <Bar dataKey="dynamoRead" fill="#8b5cf6" name="DynamoDB Reads" />
                    <Bar dataKey="errors" fill="#ef4444" name="Errors" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'ai-query' && (
          <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
            <h3 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <Search className="w-6 h-6 text-cyan-400" />
              AI-Powered Query Interface
            </h3>
            
            {backendStatus !== 'connected' && (
              <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-4 mb-4">
                <div className="flex items-start gap-2">
                  <AlertCircle className="w-5 h-5 text-yellow-400 mt-0.5" />
                  <div>
                    <div className="font-semibold text-yellow-400">Backend Connection Issue</div>
                    <div className="text-sm text-slate-300 mt-1">
                      The Java Spring Boot backend at <code className="bg-slate-900 px-1 rounded">{API_BASE_URL}</code> is not responding.
                      Please start the backend service to enable AI queries.
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            <div className="mb-6">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleAIQuery()}
                  placeholder="Ask me anything... e.g., 'Which services experienced throttling errors this week?'"
                  className="flex-1 bg-slate-700 border border-slate-600 rounded-lg px-4 py-3 focus:outline-none focus:border-cyan-500"
                />
                <button
                  onClick={handleAIQuery}
                  disabled={isProcessing}
                  className="bg-cyan-600 hover:bg-cyan-700 disabled:bg-slate-600 px-6 py-3 rounded-lg font-semibold transition-colors"
                >
                  {isProcessing ? 'Processing...' : 'Query'}
                </button>
              </div>
            </div>

            <div className="mb-6">
              <p className="text-sm text-slate-400 mb-2">Try these example queries:</p>
              <div className="flex flex-wrap gap-2">
                {[
                  'Which services experienced throttling errors this week?',
                  'List the top 5 high-impact alarms in the last 24 hours',
                  'Correlate Lambda cold starts with API latency spikes'
                ].map((example, i) => (
                  <button
                    key={i}
                    onClick={() => setQuery(example)}
                    className="bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded text-sm transition-colors"
                  >
                    {example}
                  </button>
                ))}
              </div>
            </div>

            {aiResponse && (
              <div className="bg-slate-900 rounded-lg p-6 border border-cyan-500/30">
                <div className="flex items-center gap-2 mb-3">
                  <Activity className="w-5 h-5 text-cyan-400" />
                  <span className="font-semibold text-cyan-400">AI Response</span>
                </div>
                <div className="whitespace-pre-wrap text-sm leading-relaxed">{aiResponse}</div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'alarms' && (
          <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
            <h3 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <Bell className="w-6 h-6 text-red-400" />
              Recent Alarms
            </h3>
            
            <div className="space-y-3">
              {alarms.map(alarm => (
                <div key={alarm.id} className="bg-slate-900 rounded-lg p-4 border border-slate-700">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-3">
                      <div className={`w-2 h-2 rounded-full mt-2 ${getSeverityColor(alarm.severity)}`} />
                      <div>
                        <div className="font-semibold">{alarm.service} - {alarm.metric}</div>
                        <div className="text-sm text-slate-400 mt-1">
                          <Clock className="w-3 h-3 inline mr-1" />
                          {new Date(alarm.timestamp).toLocaleString()}
                        </div>
                      </div>
                    </div>
                    <span className={`px-3 py-1 rounded text-xs font-semibold ${
                      alarm.severity === 'CRITICAL' ? 'bg-red-500/20 text-red-400' :
                      alarm.severity === 'WARNING' ? 'bg-yellow-500/20 text-yellow-400' :
                      'bg-blue-500/20 text-blue-400'
                    }`}>
                      {alarm.severity}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'correlations' && (
          <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
            <h3 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <TrendingUp className="w-6 h-6 text-cyan-400" />
              Event Correlations
            </h3>
            
            <div className="space-y-4">
              {correlations.map(corr => (
                <div key={corr.id} className="bg-slate-900 rounded-lg p-5 border border-slate-700">
                  <div className="flex items-start justify-between mb-3">
                    <div className="font-semibold text-lg">{corr.pattern}</div>
                    <div className="text-cyan-400 font-semibold">{corr.confidence}% confidence</div>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-slate-400">Occurrences:</span>
                      <span className="ml-2 font-semibold">{corr.occurrences}</span>
                    </div>
                    <div>
                      <span className="text-slate-400">Last Seen:</span>
                      <span className="ml-2 font-semibold">{corr.lastSeen}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="max-w-7xl mx-auto mt-8 bg-slate-800/50 rounded-lg p-4 border border-slate-700">
        <p className="text-xs text-slate-400">
          <strong className="text-cyan-400">Frontend-Backend Integration:</strong> React UI → REST API → Spring Boot Backend ({API_BASE_URL}) | 
          <strong className="text-cyan-400 ml-2">Stack:</strong> Java 17, Spring Boot 3.2, Kinesis (mocked), DynamoDB (mocked), OpenAI GPT-4o-mini | 
          <strong className="text-cyan-400 ml-2">Features:</strong> Real-time health checks, CORS-enabled API, AI query processing
        </p>
      </div>
    </div>
  );
};

export default RATIP;
