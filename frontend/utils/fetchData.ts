import axios from '../core/axios';

export const postAPI = async (url: string, form: object, token?: string, jwt?: string): Promise<any> => {
  try {
    const { data, headers } = await axios.post(`/${url}`, form, {
      headers: {
        'Authorization': `Basic ${token}`,
        'X-CSRF-TOKEN': jwt || '',
      }
    });
    return { data, headers };
  } catch (err: any) {
    if (err?.response) {
      return { error: err.response?.data };
    }
    console.log('Error with auth request', err.message);
  }
}

export const getAPI = async (url: string, token?: string, jwt?: string): Promise<any> => {
  try {
    const { data } = await axios.get(`/${url}`, {
      headers: {
        'Authorization': `Basic ${token}`,
        'X-CSRF-TOKEN': jwt || '',
      }
    });
    return data;
  } catch (err: any) {
    if (err?.response) {
      return { error: err.response?.data };
    }
    console.log('Error with auth request', err.message);
  }
}

export const patchAPI = async (url: string, form: object, token?: string): Promise<any> => {
  try {
    const { data } = await axios.patch(`/${url}`, form, {
      headers: { Authorization: 'Bearer ' + token, }
    });
    return data;
  } catch (err: any) {
    console.log('Error with auth request', err.message);
    if (err?.response) {
      return { error: err.response?.data };
    }
  }
}

export const deleteAPI = async (url: string, token?: string, jwt?: string): Promise<any> => {
  try {
    const { data } = await axios.delete(`/${url}`, {
      headers: {
        'Authorization': `Basic ${token}`,
        'X-CSRF-TOKEN': jwt || '',
      }
    });
    return data;
  } catch (err: any) {
    console.log('Error with auth request', err.message);
    if (err?.response) {
      return { error: err.response?.data };
    }
  }
}