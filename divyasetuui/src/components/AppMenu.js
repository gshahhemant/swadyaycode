import React from 'react';

export function AppMenu({ selectedMenu, onSelect, onLogout }) {
  return (
    <nav className="app-menu">
      <div className="menu-title">DivyaSetu Contacts</div>
      <ul>
        <li className={selectedMenu === 'vishtar' ? 'active' : ''} onClick={() => onSelect('vishtar')}>Swadhyay Kendra and Vistaar</li>
        <li className={selectedMenu === 'vratin-scheduler' ? 'active' : ''} onClick={() => onSelect('vratin-scheduler')}>Vrati</li>
        <li className={selectedMenu === 'data-processing' ? 'active' : ''} onClick={() => onSelect('data-processing')}>Data Processing</li>
      </ul>
      <button className="logout-btn" onClick={onLogout}>Logout</button>
    </nav>
  );
}
