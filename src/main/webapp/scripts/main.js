(function() {
  /**
   * Variables
   */
  let user_id = '1111';
  let user_fullname = 'John';
  let lng = -122.08;
  let lat = 37.38;
  const DEFAULT_LNG = -122.08;
  const DEFAULT_LAT = 37.38;

  // Cache DOM elements
  const loginForm = document.querySelector('#login-form');
  const registerForm = document.querySelector('#register-form');
  const itemNav = document.querySelector('#item-nav');
  const itemList = document.querySelector('#item-list');
  const avatar = document.querySelector('#avatar');
  const welcomeMsg = document.querySelector('#welcome-msg');
  const logoutBtn = document.querySelector('#logout-link');
  const loginFormBtn = document.querySelector('#login-form-btn');
  const loginBtn = document.querySelector('#login-btn');
  const registerFormBtn = document.querySelector('#register-form-btn');
  const registerBtn = document.querySelector('#register-btn');
  const nearbyBtn = document.querySelector('#nearby-btn');
  const favBtn = document.querySelector('#fav-btn');
  const recommendBtn = document.querySelector('#recommend-btn');
  const usernameInput = document.querySelector('#username');
  const passwordInput = document.querySelector('#password');
  const registerUsernameInput = document.querySelector('#register-username');
  const registerPasswordInput = document.querySelector('#register-password');
  const registerFirstNameInput = document.querySelector('#register-first-name');
  const registerLastNameInput = document.querySelector('#register-last-name');
  const loginError = document.querySelector('#login-error');
  const registerResult = document.querySelector('#register-result');

  /**
   * Initialize major event handlers
   */
  function init() {
    loginFormBtn.addEventListener('click', onSessionInvalid);
    loginBtn.addEventListener('click', login);
    registerFormBtn.addEventListener('click', showRegisterForm);
    registerBtn.addEventListener('click', register);
    nearbyBtn.addEventListener('click', loadNearbyItems);
    favBtn.addEventListener('click', loadFavoriteItems);
    recommendBtn.addEventListener('click', loadRecommendedItems);
    validateSession();
  }

  /**
   * Session
   */
  async function validateSession() {
    onSessionInvalid();
    const url = './login';

    showLoadingMessage('Validating session...');

    try {
      const res = await ajax('GET', url, null);
      const result = JSON.parse(res);

      if (result.status === 'OK') {
        onSessionValid(result);
      }
    } catch (error) {
      console.log('Login error');
    }
  }

  function onSessionValid(result) {
    user_id = result.user_id;
    user_fullname = result.name;

    welcomeMsg.innerHTML = `Welcome, ${user_fullname}`;

    showElement(itemNav);
    showElement(itemList);
    showElement(avatar);
    showElement(welcomeMsg);
    showElement(logoutBtn, 'inline-block');
    hideElement(loginForm);
    hideElement(registerForm);

    initGeoLocation();
  }

  function onSessionInvalid() {
    hideElement(itemNav);
    hideElement(itemList);
    hideElement(avatar);
    hideElement(logoutBtn);
    hideElement(welcomeMsg);
    hideElement(registerForm);

    clearLoginError();
    showElement(loginForm);
  }

  function hideElement(element) {
    element.style.display = 'none';
  }

  function showElement(element, style = 'block') {
    element.style.display = style;
  }

  function showRegisterForm() {
    hideElement(itemNav);
    hideElement(itemList);
    hideElement(avatar);
    hideElement(logoutBtn);
    hideElement(welcomeMsg);
    hideElement(loginForm);
    clearRegisterResult();
    showElement(registerForm);
  }

  function initGeoLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        onPositionUpdated,
        onLoadPositionFailed,
        { maximumAge: 60000 }
      );
      showLoadingMessage('Retrieving your location...');
    } else {
      onLoadPositionFailed();
    }
  }

  function onPositionUpdated(position) {
    lat = position.coords.latitude;
    lng = position.coords.longitude;
    loadNearbyItems();
  }

  function onLoadPositionFailed() {
    console.warn('navigator.geolocation is not available');
    getLocationFromIP();
  }

  async function getLocationFromIP() {
    const url = 'https://ipinfo.io/json';

    try {
      const res = await ajax('GET', url, null);
      const result = JSON.parse(res);
      if ('loc' in result) {
        const loc = result.loc.split(',');
        lat = parseFloat(loc[0]);
        lng = parseFloat(loc[1]);
      } else {
        console.warn('Getting location by IP failed.');
        lat = DEFAULT_LAT;
        lng = DEFAULT_LNG;
      }
      loadNearbyItems();
    } catch (error) {
      console.warn('Getting location by IP failed.');
      lat = DEFAULT_LAT;
      lng = DEFAULT_LNG;
      loadNearbyItems();
    }
  }

  // -----------------------------------
  // Login
  // -----------------------------------

  async function login() {
    let username = usernameInput.value;
    let password = passwordInput.value;
    password = md5(username + md5(password));

    const url = './login';
    const req = JSON.stringify({
      user_id: username,
      password: password,
    });

    try {
      const res = await ajax('POST', url, req);
      const result = JSON.parse(res);

      if (result.status === 'OK') {
        onSessionValid(result);
      } else {
        showLoginError();
      }
    } catch (error) {
      showLoginError();
    }
  }

  function showLoginError() {
    loginError.innerHTML = 'Invalid username or password';
  }

  function clearLoginError() {
    loginError.innerHTML = '';
  }

  // -----------------------------------
  // Register
  // -----------------------------------

  async function register() {
    let username = registerUsernameInput.value;
    let password = registerPasswordInput.value;
    let firstName = registerFirstNameInput.value;
    let lastName = registerLastNameInput.value;

    if (username === "" || password === "" || firstName === "" || lastName === "") {
      showRegisterResult('Please fill in all fields');
      return;
    }

    if (username.match(/^[a-z0-9_]+$/) === null) {
      showRegisterResult('Invalid username');
      return;
    }

    password = md5(username + md5(password));

    const url = './register';
    const req = JSON.stringify({
      user_id: username,
      password: password,
      first_name: firstName,
      last_name: lastName,
    });

    try {
      const res = await ajax('POST', url, req);
      const result = JSON.parse(res);

      if (result.status === 'OK') {
        showRegisterResult('Successfully registered');
      } else {
        showRegisterResult('User already existed');
      }
    } catch (error) {
      showRegisterResult('Failed to register');
    }
  }

  function showRegisterResult(registerMessage) {
    registerResult.innerHTML = registerMessage;
  }

  function clearRegisterResult() {
    registerResult.innerHTML = '';
  }

  // -----------------------------------
  // Helper Functions
  // -----------------------------------

  function activeBtn(btnId) {
    const btns = document.querySelectorAll('.main-nav-btn');
    btns.forEach(btn => btn.classList.remove('active'));

    const btn = document.querySelector('#' + btnId);
    btn.classList.add('active');
  }

  function showLoadingMessage(msg) {
    itemList.innerHTML = `<p class="notice"><i class="fa fa-spinner fa-spin"></i> ${msg}</p>`;
  }

  function showWarningMessage(msg) {
    itemList.innerHTML = `<p class="notice"><i class="fa fa-exclamation-triangle"></i> ${msg}</p>`;
  }

  function showErrorMessage(msg) {
    itemList.innerHTML = `<p class="notice"><i class="fa fa-exclamation-circle"></i> ${msg}</p>`;
  }

  function $create(tag, options) {
    const element = document.createElement(tag);
    for (let key in options) {
      if (options.hasOwnProperty(key)) {
        element[key] = options[key];
      }
    }
    return element;
  }

  /**
   * AJAX helper using Fetch API
   */
  function ajax(method, url, data) {
    const options = {
      method: method,
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    };

    if (method === 'GET' || method === 'DELETE') {
      delete options.headers['Content-Type'];
    }

    if (data !== null) {
      options.body = data;
    }

    return fetch(url, options).then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.text();
    });
  }

  // -------------------------------------
  // AJAX call server-side APIs
  // -------------------------------------

  /**
   * API #1 Load the nearby items
   */
  async function loadNearbyItems() {
    activeBtn('nearby-btn');
    const url = './search';
    const params = `user_id=${user_id}&lat=${lat}&lon=${lng}`;

    showLoadingMessage('Loading nearby items...');

    try {
      const res = await ajax('GET', `${url}?${params}`, null);
      const items = JSON.parse(res);
      if (!items || items.length === 0) {
        showWarningMessage('No nearby item.');
      } else {
        listItems(items);
      }
    } catch (error) {
      showErrorMessage('Cannot load nearby items.');
    }
  }

  /**
   * API #2 Load favorite items
   */
  async function loadFavoriteItems() {
    activeBtn('fav-btn');
    const url = './history';
    const params = `user_id=${user_id}`;

    showLoadingMessage('Loading favorite items...');

    try {
      const res = await ajax('GET', `${url}?${params}`, null);
      const items = JSON.parse(res);
      if (!items || items.length === 0) {
        showWarningMessage('No favorite item.');
      } else {
        listItems(items);
      }
    } catch (error) {
      showErrorMessage('Cannot load favorite items.');
    }
  }

  /**
   * API #3 Load recommended items
   */
  async function loadRecommendedItems() {
    activeBtn('recommend-btn');
    const url = `./recommendation?user_id=${user_id}&lat=${lat}&lon=${lng}`;

    showLoadingMessage('Loading recommended items...');

    try {
      const res = await ajax('GET', url, null);
      const items = JSON.parse(res);
      if (!items || items.length === 0) {
        showWarningMessage('No recommended item. Make sure you have favorites.');
      } else {
        listItems(items);
      }
    } catch (error) {
      showErrorMessage('Cannot load recommended items.');
    }
  }

  /**
   * API #4 Toggle favorite items
   */
  async function changeFavoriteItem(item) {
    const li = document.querySelector('#item-' + item.item_id);
    const favIcon = document.querySelector('#fav-icon-' + item.item_id);
    const favorite = !(li.dataset.favorite === 'true');

    const url = './history';
    const req = JSON.stringify({
      user_id: user_id,
      favorite: item
    });
    const method = favorite ? 'POST' : 'DELETE';

    try {
      const res = await ajax(method, url, req);
      const result = JSON.parse(res);
      if (result.status === 'OK' || result.result === 'SUCCESS') {
        li.dataset.favorite = favorite;
        favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
      }
    } catch (error) {
      console.error('Failed to change favorite status');
    }
  }

  // -------------------------------------
  // Create item list
  // -------------------------------------

  function listItems(items) {
    itemList.innerHTML = '';

    for (let i = 0; i < items.length; i++) {
      addItem(itemList, items[i]);
    }
  }

  function addItem(itemList, item) {
    const item_id = item.item_id;

    const li = $create('li', {
      id: 'item-' + item_id,
      className: 'item',
      dataset: { item_id: item_id, favorite: item.favorite }
    });

    const imgSrc = item.image_url || 'https://via.placeholder.com/100';
    li.appendChild($create('img', { src: imgSrc }));

    const section = $create('div');

    const title = $create('a', {
      className: 'item-name',
      href: item.url,
      target: '_blank',
      innerHTML: item.name
    });
    section.appendChild(title);

    const keyword = $create('p', {
      className: 'item-keyword',
      innerHTML: 'Keyword: ' + item.keywords.join(', ')
    });
    section.appendChild(keyword);

    li.appendChild(section);

    const address = $create('p', {
      className: 'item-address',
      innerHTML: item.address.replace(/,/g, '<br/>').replace(/\"/g, '')
    });
    li.appendChild(address);

    const favLink = $create('p', {
      className: 'fav-link',
      onclick: () => changeFavoriteItem(item)
    });

    favLink.appendChild($create('i', {
      id: 'fav-icon-' + item_id,
      className: item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
    }));

    li.appendChild(favLink);
    itemList.appendChild(li);
  }

  document.addEventListener('DOMContentLoaded', init);
})();
