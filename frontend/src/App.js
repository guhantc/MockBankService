import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './App.css';
import { HDFCBankHomePage } from './components';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HDFCBankHomePage />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;