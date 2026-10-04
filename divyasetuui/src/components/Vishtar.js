import React, { useState, useEffect } from 'react';
import { API_ENDPOINTS } from '../config/api';
import { YearInput } from './YearInput';
import { ContactsTable } from './ContactsTable';
import { AddContactForm } from './AddContactForm';
import { useNotification, Notification } from './useNotification';

export function Vishtar({ user }) {
  const [kendraNames, setKendraNames] = useState([]);
  const [selectedKendra, setSelectedKendra] = useState('');
  const [visharList, setVisharList] = useState([]);
  const [selectedVishar, setSelectedVishar] = useState('');
  const [communities, setCommunities] = useState([]);
  const [selectedCommunity, setSelectedCommunity] = useState('');
  const [selectedCommunityZip, setSelectedCommunityZip] = useState('');
  const [zipCity, setZipCity] = useState([]);
  const [year, setYear] = useState('2026');
  const [contacts, setContacts] = useState([]);
  const [nameFilter, setNameFilter] = useState('');
  const [addressFilter, setAddressFilter] = useState('');
  const [showAddContactForm, setShowAddContactForm] = useState(false);
  const { notification, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    fetch(API_ENDPOINTS.KENDRA_NAMES)
      .then(res => res.json())
      .then(data => setKendraNames(Array.isArray(data) ? data : []))
      .catch(err => console.error('Kendra names fetch error:', err));
    fetch(API_ENDPOINTS.ZIP_AND_CITY)
      .then(res => res.json())
      .then(data => setZipCity(Array.isArray(data) ? data : []))
      .catch(err => console.error('Zip-city fetch error:', err));
  }, []);

  useEffect(() => {
    const url = selectedKendra
      ? `${API_ENDPOINTS.VISHAR_AND_ZIP}?kendraName=${encodeURIComponent(selectedKendra)}`
      : API_ENDPOINTS.VISHAR_AND_ZIP;
    fetch(url)
      .then(res => res.json())
      .then(data => {
        setVisharList(Array.isArray(data) ? data : []);
        setSelectedVishar('');
        setCommunities([]);
        setSelectedCommunity('');
        setContacts([]);
      })
      .catch(err => console.error('Vishar fetch error:', err));
  }, [selectedKendra]);

  useEffect(() => {
    if (selectedVishar) {
      fetch(`${API_ENDPOINTS.COMMUNITIES}/vishar/${encodeURIComponent(selectedVishar)}`)
        .then(res => res.json())
        .then(data => {
          console.log('Community API response:', data);
          setCommunities(Array.isArray(data) ? data : []);
          setSelectedCommunity('');
          setSelectedCommunityZip('');
          setContacts([]);
        })
        .catch(err => console.error('Community fetch error:', err));
    } else {
      setCommunities([]);
      setSelectedCommunity('');
      setContacts([]);
    }
  }, [selectedVishar]);

  const getZipForSelected = () => selectedCommunityZip;

  const getCommunityName = () => selectedCommunity.split('___')[0];

  const fetchContacts = () => {
    const zip = getZipForSelected();
    const communityName = getCommunityName();
    if (!zip || !communityName || !year) {
      alert('Please select Vistaar, Community and enter Year.');
      return;
    }
    const url = `${API_ENDPOINTS.CONTACTS}/zip/${zip}/community/${communityName}?commentYear=${year}`;
    fetch(url)
      .then(res => res.json())
      .then(data => {
        const editable = data.map(contact => {
          const notes = {};
          contact.comments?.forEach(c => {
            if (c.year === year) {
              notes[c.month] = c.note;
            }
          });
          return { ...contact, editableNotes: notes, originalNotes: { ...notes } };
        });
        setContacts(editable);
      })
      .catch(err => console.error('Contact fetch error:', err));
  };

  const handleNoteChange = (contactIndex, month, value) => {
    setContacts(prev => {
      const updated = [...prev];
      updated[contactIndex].editableNotes = {
        ...updated[contactIndex].editableNotes,
        [month]: value
      };
      return updated;
    });
  };

  const handleUpdate = (contact) => {
    const hasChanged = Object.keys(contact.editableNotes || {}).some(
      month => contact.editableNotes[month] !== contact.originalNotes?.[month]
    );
    if (!hasChanged) {
      alert('No changes detected for this contact.');
      return;
    }
    const zip = getZipForSelected();
    const comments = Object.entries(contact.editableNotes)
      .filter(([_, note]) => note?.trim())
      .map(([month, note]) => ({ year, month, note }));
    const updatedContact = {
      property_id: contact.property_id,
      name: contact.name,
      property_address: contact.property_address,
      community_name: getCommunityName(),
      zip,
      comments,
      user_id: user?.username || '',
      updated_by: user?.username || ''
    };
    fetch(`${API_ENDPOINTS.UPDATE_CONTACTS}?commentYear=${year}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify([updatedContact])
    })
      .then(res => {
        if (!res.ok) throw new Error('Update failed');
        alert(`Updated: ${contact.property_id}`);
      })
      .catch(err => alert(`Error updating contact: ${err}`));
  };

  const handleBulkUpdate = () => {
    if (!(user?.roles === 'S' || user?.roles === 'W')) {
      alert('You do not have permission to perform bulk update.');
      return;
    }
    if (!year) {
      alert('Please enter a year first.');
      return;
    }
    const zip = getZipForSelected();
    const contactsToUpdate = contacts
      .filter(contact =>
        Object.keys(contact.editableNotes || {}).some(
          month => contact.editableNotes[month] !== contact.originalNotes?.[month]
        )
      )
      .map(contact => {
        const comments = Object.entries(contact.editableNotes || {})
          .filter(([_, note]) => note?.trim())
          .map(([month, note]) => ({ year, month, note }));
        return {
          property_id: contact.property_id,
          name: contact.name,
          property_address: contact.property_address,
          community_name: getCommunityName(),
          zip,
          comments,
          user_id: user?.username || '',
          updated_by: user?.username || ''
        };
      });
    if (contactsToUpdate.length === 0) {
      alert('No notes to update.');
      return;
    }
    fetch(`${API_ENDPOINTS.UPDATE_CONTACTS}?commentYear=${year}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contactsToUpdate)
    })
      .then(res => {
        if (!res.ok) throw new Error('Bulk update failed');
        alert('Bulk update successful!');
      })
      .catch(err => alert(`Bulk update error: ${err}`));
  };

  const handleDownload = () => {
    const zip = getZipForSelected();
    const communityName = getCommunityName();
    if (!zip || !communityName || !year) {
      alert('Please select Vistaar, Community and enter Year.');
      return;
    }
    const downloadUrl = `${API_ENDPOINTS.DIVYASETU}/api/folder/zip/${zip}/community/${communityName}?commentYear=${year}`;
    fetch(downloadUrl)
      .then(response => {
        if (!response.ok) throw new Error('Failed to download file.');
        return response.blob();
      })
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${getCommunityName()}.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
      .catch(err => alert('Download error: ' + err.message));
  };

  const handleDownloadCommunityExcel = () => {
    const zip = getZipForSelected();
    const communityName = getCommunityName();
    if (!zip || !communityName || !year) {
      alert('Please select Vistaar, Community and enter Year.');
      return;
    }

    const downloadUrl = `${API_ENDPOINTS.DOWNLOAD_COMMUNITY_EXCEL}/${zip}/community/${communityName}/withdetails?commentYear=${year}`;
    fetch(downloadUrl)
      .then(response => {
        if (!response.ok) throw new Error('Failed to download community Excel file.');
        return response.blob();
      })
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${communityName}-withdetails.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
      .catch(err => alert('Community download error: ' + err.message));
  };

  return (
    <>
      <div style={{ display: 'flex', gap: '12px', marginBottom: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
        <label style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ fontSize: '12px', fontWeight: 600, whiteSpace: 'nowrap' }}>Swadhyay Kendra:</span>
          <select value={selectedKendra} onChange={e => setSelectedKendra(e.target.value)}>
            <option value="">--Select Kendra--</option>
            {kendraNames.map((k, idx) => <option key={idx} value={k}>{k}</option>)}
          </select>
        </label>
        <label style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ fontSize: '12px', fontWeight: 600, whiteSpace: 'nowrap' }}>Vistaar:</span>
          <select value={selectedVishar} onChange={e => setSelectedVishar(e.target.value)}>
            <option value="">--Select Vistaar--</option>
            {visharList.map((v, idx) => <option key={idx} value={v}>{v}</option>)}
          </select>
        </label>
        <label style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ fontSize: '12px', fontWeight: 600, whiteSpace: 'nowrap' }}>Community:</span>
          <select
            value={selectedCommunity}
            onChange={e => {
              const val = e.target.value;
              setSelectedCommunity(val);
              const parts = val.split('___');
              const zip = parts[1] || '';
              if (zip) {
                setSelectedCommunityZip(zip);
              } else {
                const comm = communities.find(c => c.communityname === parts[0]);
                setSelectedCommunityZip(comm?.zipcode || '');
              }
              setContacts([]);
            }}
          >
            <option value="">--Select Community--</option>
            {communities.map((c, idx) => {
              const isAll = c.communityname === 'ALL COMMUNITIES';
              const value = isAll ? `ALL COMMUNITIES___${c.zipcode}` : c.communityname;
              let label = c.communityname;
              if (isAll) {
                const cityObj = zipCity.find(z => z.zipcode === c.zipcode);
                const city = cityObj ? cityObj.cityname : '';
                label = `ALL COMMUNITIES - ${city} ${c.zipcode}`;
              }
              return <option key={idx} value={value}>{label}</option>;
            })}
          </select>
        </label>
        <YearInput year={year} onChange={setYear} />
      </div>
      <div style={{ display: 'flex', gap: '8px', marginBottom: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
        <button className="action-btn-sm" onClick={fetchContacts}>Get Contacts</button>
        {(user?.roles === 'W' || user?.roles === 'S') && (
          <button className="action-btn-sm" onClick={() => setShowAddContactForm(true)}>Add New Contact</button>
        )}
        {contacts.length > 0 && (
          <>
            {(user?.roles === 'S' || user?.roles === 'W') && (
              <button className="action-btn-sm" onClick={handleBulkUpdate}>🔁 Bulk Update All</button>
            )}
            <button className="action-btn-sm" onClick={handleDownload}>📥 Download Excel</button>
            <button className="action-btn-sm" onClick={handleDownloadCommunityExcel}>📥 Download Community Excel</button>
            <span style={{ fontSize: '13px', color: '#718096', background: '#edf2f7', borderRadius: '12px', padding: '3px 12px', fontWeight: 500 }}>
              🏘️ <strong>{contacts.length}</strong> contacts in <strong>{getCommunityName()}</strong>
            </span>
          </>
        )}
      </div>
      <ContactsTable
            contacts={contacts}
            nameFilter={nameFilter}
            addressFilter={addressFilter}
            onNoteChange={handleNoteChange}
            onUpdate={handleUpdate}
            setNameFilter={setNameFilter}
            setAddressFilter={setAddressFilter}
            userRole={user?.roles}
            stickyHeader={true}
          />
      {showAddContactForm && (
        <AddContactForm
          selectedZip={getZipForSelected()}
          selectedCommunity={getCommunityName()}
          user={user}
          onClose={() => setShowAddContactForm(false)}
          onSave={() => { if (contacts.length > 0) fetchContacts(); }}
          showNotification={showNotification}
        />
      )}
      <Notification notification={notification} onClose={hideNotification} />
    </>
  );
}
