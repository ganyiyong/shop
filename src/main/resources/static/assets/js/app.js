(function () {
  function openModal(id, trigger) {
    var modal = document.getElementById(id);
    if (!modal) return;
    var form = modal.querySelector('form');
    if (form) {
      form.reset();
      form.querySelectorAll('input[checked]').forEach(function (input) { input.checked = true; });
      form.querySelectorAll('[data-field]').forEach(function (field) {
        var key = field.getAttribute('data-field');
        if (trigger && trigger.dataset[key] !== undefined) field.value = trigger.dataset[key] || '';
        else if (field.type === 'hidden') field.value = '';
      });
      form.querySelectorAll('select').forEach(function (select) {
        select.dispatchEvent(new Event('searchselect:refresh'));
      });
    }
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.classList.add('modal-open');
  }
  function closeModal(target) {
    var modal = target.closest('.modal');
    if (!modal) return;
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    if (!document.querySelector('.modal.open')) document.body.classList.remove('modal-open');
  }
  document.querySelectorAll('[data-modal-open]').forEach(function (button) {
    button.addEventListener('click', function () { openModal(button.getAttribute('data-modal-open'), button); });
  });
  document.querySelectorAll('[data-modal-close]').forEach(function (button) {
    button.addEventListener('click', function () { closeModal(button); });
  });
  document.querySelectorAll('[data-auto-submit]').forEach(function (input) {
    input.addEventListener('change', function () {
      var form = input.closest('form');
      if (form) form.submit();
    });
  });

  function setupSearchableGoodsSelects() {
    var selector = [
      'select[name="goodsId"]',
      'select[data-sale-goods]',
      'select[data-field="goodsid"]'
    ].join(',');

    function normalize(value) {
      return (value || '').toLowerCase().replace(/\s+/g, '');
    }

    function refresh(select) {
      var combo = select.nextElementSibling;
      if (!combo || !combo.classList.contains('search-select')) return;
      var button = combo.querySelector('[data-search-select-button]');
      var selected = select.options[select.selectedIndex];
      if (button) button.textContent = selected ? selected.textContent : '';
      combo.querySelectorAll('[data-search-select-option]').forEach(function (item) {
        item.classList.toggle('selected', item.dataset.value === select.value);
      });
    }

    function closeAll(except) {
      document.querySelectorAll('.search-select.open').forEach(function (combo) {
        if (combo !== except) combo.classList.remove('open');
      });
    }

    function alignPanel(combo) {
      var panel = combo.querySelector('.search-select-panel');
      if (!panel) return;
      panel.style.right = 'auto';
      panel.style.left = '0';
      var rect = panel.getBoundingClientRect();
      if (rect.right > window.innerWidth - 12) {
        panel.style.left = 'auto';
        panel.style.right = '0';
      }
    }

    document.querySelectorAll(selector).forEach(function (select) {
      if (select.dataset.searchReady === 'true') return;
      select.dataset.searchReady = 'true';

      var combo = document.createElement('div');
      combo.className = 'search-select';
      var button = document.createElement('button');
      button.type = 'button';
      button.className = 'search-select-button';
      button.setAttribute('data-search-select-button', '');
      button.setAttribute('aria-haspopup', 'listbox');
      var panel = document.createElement('div');
      panel.className = 'search-select-panel';
      var input = document.createElement('input');
      input.type = 'search';
      input.className = 'search-select-input';
      input.placeholder = '搜索商品';
      input.setAttribute('data-search-select-input', '');
      var list = document.createElement('div');
      list.className = 'search-select-list';
      list.setAttribute('role', 'listbox');
      var empty = document.createElement('div');
      empty.className = 'search-select-empty';
      empty.textContent = '没有匹配的商品';

      Array.prototype.slice.call(select.options).forEach(function (option) {
        var item = document.createElement('button');
        item.type = 'button';
        item.className = 'search-select-option';
        item.textContent = option.textContent;
        item.dataset.value = option.value;
        item.dataset.search = normalize(option.textContent);
        item.setAttribute('data-search-select-option', '');
        item.setAttribute('role', 'option');
        item.addEventListener('click', function () {
          select.value = option.value;
          select.dispatchEvent(new Event('change', { bubbles: true }));
          combo.classList.remove('open');
          refresh(select);
        });
        list.appendChild(item);
      });

      panel.appendChild(input);
      panel.appendChild(list);
      panel.appendChild(empty);
      combo.appendChild(button);
      combo.appendChild(panel);
      select.insertAdjacentElement('afterend', combo);
      select.classList.add('native-search-select');

      function filter() {
        var keyword = normalize(input.value);
        var count = 0;
        list.querySelectorAll('[data-search-select-option]').forEach(function (item) {
          var visible = !keyword || item.dataset.search.indexOf(keyword) !== -1;
          item.hidden = !visible;
          if (visible) count += 1;
        });
        empty.style.display = count ? 'none' : 'block';
      }

      button.addEventListener('click', function () {
        var open = !combo.classList.contains('open');
        closeAll(combo);
        combo.classList.toggle('open', open);
        if (open) {
          input.value = '';
          filter();
          alignPanel(combo);
          input.focus();
        }
      });
      input.addEventListener('input', filter);
      input.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') combo.classList.remove('open');
        if (event.key === 'Enter') {
          var first = list.querySelector('[data-search-select-option]:not([hidden])');
          if (first) {
            event.preventDefault();
            first.click();
          }
        }
      });
      select.addEventListener('change', function () { refresh(select); });
      select.addEventListener('searchselect:refresh', function () { refresh(select); });
      refresh(select);
      filter();
    });

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.search-select')) closeAll();
    });
  }

  document.querySelectorAll('[data-sales-view-submit]').forEach(function (button) {
    button.addEventListener('click', function () {
      var form = button.closest('form');
      var viewInput = form && form.querySelector('[data-sales-view]');
      if (viewInput) viewInput.value = button.dataset.salesViewSubmit || 'detail';
    });
  });
  function closeMobileMenu() {
    document.body.classList.remove('drawer-open');
  }
  var mobileMenuButton = document.querySelector('[data-mobile-menu]');
  var mobileMask = document.querySelector('[data-mobile-mask]');
  var mobileCloseButton = document.querySelector('[data-mobile-close]');
  if (mobileMenuButton) {
    mobileMenuButton.addEventListener('click', function () {
      document.body.classList.toggle('drawer-open');
    });
  }
  if (mobileMask) mobileMask.addEventListener('click', closeMobileMenu);
  if (mobileCloseButton) mobileCloseButton.addEventListener('click', closeMobileMenu);
  document.querySelectorAll('.sidebar .nav a,.sidebar .logout').forEach(function (link) {
    link.addEventListener('click', closeMobileMenu);
  });
  document.querySelectorAll('[data-mobile-collapse]').forEach(function (panel) {
    var button = panel.querySelector('[data-mobile-collapse-toggle]');
    if (!button) return;
    button.addEventListener('click', function () {
      var isOpen = panel.classList.toggle('open');
      button.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
      var hint = button.querySelector('small');
      if (hint) hint.textContent = hint.textContent.replace(isOpen ? '点击展开' : '点击收起', isOpen ? '点击收起' : '点击展开');
    });
  });
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      document.querySelectorAll('.modal.open,.date-popover.open').forEach(function (item) { item.classList.remove('open'); });
      document.body.classList.remove('modal-open');
      closeMobileMenu();
    }
  });
  document.querySelectorAll('input[type="number"]').forEach(function (input) {
    input.addEventListener('wheel', function () { if (document.activeElement === input) input.blur(); });
  });

  function pad(n) { return n < 10 ? '0' + n : String(n); }
  function fmt(date) { return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()); }
  function parseDate(value) {
    var parts = (value || '').split('-').map(Number);
    return parts.length === 3 ? new Date(parts[0], parts[1] - 1, parts[2]) : new Date();
  }
  function setupDatepickerRange() {
    var startInput = document.querySelector('[data-datepicker-start]');
    var endInput = document.querySelector('[data-datepicker-end]');
    if (!startInput || !endInput) return;
    function normalize() {
      if (!startInput.value || !endInput.value) return;
      var start = parseDate(startInput.value);
      var end = parseDate(endInput.value);
      if (start > end) {
        if (document.activeElement === startInput) endInput.value = startInput.value;
        else startInput.value = endInput.value;
      }
    }
    startInput.addEventListener('change', normalize);
    endInput.addEventListener('change', normalize);
  }

  function setupSalePlatform() {
    var platformInput = document.querySelector('[data-sale-platform]');
    var platformView = document.querySelector('[data-sale-platform-view]');
    var shopInputs = document.querySelectorAll('input[name="shop"][data-platform]');
    if (!platformInput || shopInputs.length === 0) return;
    function syncPlatform() {
      var checked = document.querySelector('input[name="shop"][data-platform]:checked');
      var platform = checked ? checked.dataset.platform : '';
      platformInput.value = platform;
      if (platformView) platformView.value = platform;
    }
    shopInputs.forEach(function (input) {
      input.addEventListener('change', syncPlatform);
    });
    syncPlatform();
  }

  function setupStockPicker() {
    var goodsSelect = document.querySelector('[data-sale-goods]');
    var stockIdInput = document.querySelector('[data-sale-stock-id]');
    var costInput = document.querySelector('[data-sale-cost]');
    var outCostInput = document.querySelector('[data-sale-out-cost]');
    var salePriceInput = document.querySelector('[data-sale-price]');
    var chargeInput = document.querySelector('[data-sale-charge]');
    var profitInput = document.querySelector('[data-sale-profit]');
    var stockLabel = document.querySelector('[data-sale-stock-label]');
    var stockModal = document.getElementById('stockPickerModal');
    if (!goodsSelect || !stockIdInput || !stockModal) return;
    var saleForm = goodsSelect.closest('form');

    function money(value) {
      var number = Number(value || 0);
      return Number.isFinite(number) ? number : 0;
    }
    function fixed(value) {
      return (Math.round(money(value) * 100) / 100).toFixed(2);
    }
    function recalcSale() {
      var sellingPrice = money(salePriceInput && salePriceInput.value);
      var outCostPrice = money(outCostInput && outCostInput.value);
      var charge = sellingPrice * 0.006;
      if (chargeInput) chargeInput.value = fixed(charge);
      if (profitInput) profitInput.value = fixed(sellingPrice - outCostPrice - charge);
    }
    function resetStock() {
      stockIdInput.value = '';
      if (costInput) costInput.value = '';
      if (outCostInput) outCostInput.value = '';
      if (salePriceInput) salePriceInput.value = '';
      if (chargeInput) chargeInput.value = '';
      if (profitInput) profitInput.value = '';
      if (stockLabel) stockLabel.textContent = '未选择库存批次';
    }
    function filterRows() {
      var goodsId = goodsSelect.value;
      stockModal.querySelectorAll('[data-picker-goods]').forEach(function (row) {
        row.style.display = !goodsId || row.dataset.pickerGoods === goodsId ? '' : 'none';
      });
    }
    goodsSelect.addEventListener('change', function () {
      resetStock();
      filterRows();
    });
    document.querySelectorAll('[data-modal-open="stockPickerModal"]').forEach(function (button) {
      button.addEventListener('click', filterRows);
    });
    stockModal.querySelectorAll('[data-stock-pick]').forEach(function (button) {
      button.addEventListener('click', function () {
        stockIdInput.value = button.dataset.stockId || '';
        goodsSelect.value = button.dataset.goodsId || goodsSelect.value;
        goodsSelect.dispatchEvent(new Event('searchselect:refresh'));
        if (costInput) costInput.value = button.dataset.stockPrice || '';
        if (outCostInput) outCostInput.value = button.dataset.stockCost || '';
        if (salePriceInput) salePriceInput.value = button.dataset.stockSelling || '';
        if (stockLabel) {
          stockLabel.textContent = (button.dataset.stockName || '库存批次') + ' / 库存 ' + (button.dataset.stockCount || '0') + ' / 单价 ' + (button.dataset.stockPrice || '0');
        }
        recalcSale();
        stockModal.classList.remove('open');
        stockModal.setAttribute('aria-hidden', 'true');
      });
    });
    if (salePriceInput) salePriceInput.addEventListener('input', recalcSale);
    if (salePriceInput) salePriceInput.addEventListener('change', recalcSale);
    if (outCostInput) outCostInput.addEventListener('input', recalcSale);
    if (outCostInput) outCostInput.addEventListener('change', recalcSale);
    if (saleForm) {
      saleForm.addEventListener('submit', function (event) {
        if (!stockIdInput.value) {
          event.preventDefault();
          alert('请先选择本次销售要扣除的库存批次');
          return;
        }
        recalcSale();
      });
    }
    filterRows();
  }

  function setupMonthPicker() {
    var trigger = document.querySelector('[data-month-picker]');
    var popover = document.querySelector('[data-month-popover]');
    if (!trigger || !popover) return;
    var form = trigger.closest('form');
    var valueInput = form.querySelector('[data-month-value]');
    var label = form.querySelector('[data-month-label]');
    var title = popover.querySelector('[data-month-title]');
    var grid = popover.querySelector('[data-month-grid]');
    var selected = valueInput.value || fmt(new Date()).slice(0, 7);
    var year = Number(selected.slice(0, 4)) || new Date().getFullYear();
    function renderMonth() {
      title.textContent = year + '年';
      var html = '';
      for (var i = 1; i <= 12; i++) {
        var value = year + '-' + pad(i);
        html += '<button type="button" class="' + (value === selected ? 'selected' : '') + '" data-month-option="' + value + '">' + i + '月</button>';
      }
      grid.innerHTML = html;
      valueInput.value = selected;
      label.textContent = Number(selected.slice(0, 4)) + '年 ' + Number(selected.slice(5, 7)) + '月';
    }
    function submitMonth() {
      valueInput.value = selected;
      popover.classList.remove('open');
      form.submit();
    }
    trigger.addEventListener('click', function () {
      var rect = trigger.getBoundingClientRect();
      popover.style.left = Math.max(12, rect.left + window.scrollX) + 'px';
      popover.style.top = rect.bottom + window.scrollY + 8 + 'px';
      popover.classList.toggle('open');
      renderMonth();
    });
    popover.addEventListener('click', function (event) {
      var option = event.target.closest('[data-month-option]');
      if (option) {
        selected = option.dataset.monthOption;
        year = Number(selected.slice(0, 4));
        renderMonth();
        submitMonth();
      }
      if (event.target.matches('[data-month-prev]')) { year -= 1; renderMonth(); }
      if (event.target.matches('[data-month-next]')) { year += 1; renderMonth(); }
      if (event.target.matches('[data-month-now]')) {
        selected = fmt(new Date()).slice(0, 7);
        year = Number(selected.slice(0, 4));
        submitMonth();
      }
      if (event.target.matches('[data-month-apply]')) submitMonth();
    });
    document.addEventListener('click', function (event) {
      if (!popover.contains(event.target) && !trigger.contains(event.target)) popover.classList.remove('open');
    });
  }

  function setupDashboardCharts() {
    function money(value) {
      var number = Number(value || 0);
      return Number.isFinite(number) ? number : 0;
    }
    function readPoints(canvas) {
      var box = canvas.parentElement;
      return Array.prototype.slice.call(box.querySelectorAll('[data-chart-point]')).reverse().map(function (item) {
        return { label: item.dataset.label || '', value: money(item.dataset.value), stock: money(item.dataset.stock) };
      });
    }
    function escapeHtml(value) {
      return String(value || '').replace(/[&<>"']/g, function (char) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char];
      });
    }
    function monthKey(date) {
      return date.getFullYear() + '-' + pad(date.getMonth() + 1);
    }
    function recentMonths(source) {
      var map = {};
      source.forEach(function (p) { map[p.label] = p.value; });
      var now = new Date();
      var months = [];
      for (var i = 11; i >= 0; i--) {
        var date = new Date(now.getFullYear(), now.getMonth() - i, 1);
        var key = monthKey(date);
        months.push({ label: key, value: map[key] || 0 });
      }
      return months;
    }
    function shortLabel(label) {
      if (!label) return '';
      var parts = label.split('-');
      return parts.length > 1 ? Number(parts[1]) + '月' : label.slice(0, 5);
    }
    function shortMoney(value) {
      if (value >= 10000) return Math.round(value / 10000) + '万';
      return Math.round(value).toLocaleString();
    }
    function fullMoney(value) {
      return '￥' + money(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    function renderMonthBars(container) {
      var points = recentMonths(readPoints(container));
      var max = Math.max.apply(null, points.map(function (p) { return p.value; })) || 1;
      container.innerHTML = points.map(function (p) {
        var height = Math.max(8, Math.round((p.value / max) * 140));
        var label = p.value >= 10000 ? shortMoney(p.value) : Math.round(p.value).toLocaleString();
        return '<div class="month-bar ' + (p.value > 0 ? 'has-label' : '') + '" style="--bar-height:' + height + 'px">' +
          '<div class="month-bar-track"><strong>' + label + '</strong><div class="month-bar-fill" style="height:' + height + 'px"></div></div>' +
          '<span>' + shortLabel(p.label) + '</span>' +
          '</div>';
      }).join('');
    }
    function sortRankPoints(points, key, dir) {
      var multiplier = dir === 'asc' ? 1 : -1;
      return points.slice().sort(function (a, b) {
        var diff = (a[key] || 0) - (b[key] || 0);
        if (diff === 0) diff = (a.label || '').localeCompare(b.label || '', 'zh-Hans-CN');
        return diff * multiplier;
      });
    }
    function renderRankBars(container) {
      var key = container.dataset.rankSort || 'value';
      var dir = container.dataset.rankDir || 'desc';
      var points = sortRankPoints(readPoints(container), key, dir);
      var max = Math.max.apply(null, points.map(function (p) { return p[key]; })) || 1;
      container.innerHTML = points.map(function (p) {
        var width = Math.max(4, Math.round((p[key] / max) * 100));
        var label = p.label.length > 12 ? p.label.slice(0, 12) + '…' : p.label;
        return '<div class="rank-bar">' +
          '<div class="rank-bar-head"><span>' + escapeHtml(label) + '</span><b>' + fullMoney(p.value) + ' / ' + Math.round(p.stock).toLocaleString() + '件</b></div>' +
          '<div class="rank-track"><div class="rank-fill" style="width:' + width + '%"></div></div>' +
          '</div>';
      }).join('');
    }
    function renderRankList(list) {
      var panel = list.closest('.panel');
      var chart = panel && panel.querySelector('[data-rank-chart]');
      if (!chart) return;
      var key = chart.dataset.rankSort || 'value';
      var dir = chart.dataset.rankDir || 'desc';
      var points = sortRankPoints(readPoints(chart), key, dir);
      list.innerHTML = points.map(function (p) {
        var percent = key === 'stock'
          ? Math.round(p.stock).toLocaleString() + ' 件'
          : fullMoney(p.value);
        return '<div class="rank-line">' +
          '<span>' + escapeHtml(p.label) + '</span>' +
          '<b><small>' + escapeHtml(percent) + '</small>' + Math.round(p.stock).toLocaleString() + ' 件 / ' + fullMoney(p.value) + '</b>' +
          '</div>';
      }).join('') || '<div class="empty block">暂无库存数据</div>';
    }
    function setupRankSortControls() {
      var chart = document.querySelector('[data-rank-chart]');
      var list = document.querySelector('[data-rank-list]');
      if (!chart) return;
      chart.dataset.rankSort = chart.dataset.rankSort || 'value';
      chart.dataset.rankDir = chart.dataset.rankDir || 'desc';
      function syncButtons(selector, value) {
        document.querySelectorAll(selector).forEach(function (button) {
          button.classList.toggle('active', button.dataset.rankSort === value);
        });
      }
      function syncDirButton() {
        var button = document.querySelector('[data-rank-dir-toggle]');
        if (!button) return;
        var desc = chart.dataset.rankDir !== 'asc';
        var icon = button.querySelector('[data-rank-dir-icon]');
        button.dataset.rankDir = desc ? 'desc' : 'asc';
        button.title = desc ? '从高到低' : '从低到高';
        button.setAttribute('aria-label', button.title);
        if (icon) icon.setAttribute('d', desc ? 'M12 5v14M6.5 13.5 12 19l5.5-5.5' : 'M12 19V5M6.5 10.5 12 5l5.5 5.5');
      }
      function refreshRank() {
        syncButtons('[data-rank-sort]', chart.dataset.rankSort);
        syncDirButton();
        renderRankBars(chart);
        if (list) renderRankList(list);
      }
      document.querySelectorAll('[data-rank-sort]').forEach(function (button) {
        button.addEventListener('click', function () {
          chart.dataset.rankSort = button.dataset.rankSort || 'value';
          refreshRank();
        });
      });
      var dirButton = document.querySelector('[data-rank-dir-toggle]');
      if (dirButton) dirButton.addEventListener('click', function () {
        chart.dataset.rankDir = chart.dataset.rankDir === 'asc' ? 'desc' : 'asc';
        refreshRank();
      });
      refreshRank();
    }
    function renderCharts() {
      document.querySelectorAll('[data-month-chart]').forEach(renderMonthBars);
      document.querySelectorAll('[data-rank-list]').forEach(renderRankList);
      document.querySelectorAll('[data-rank-chart]').forEach(renderRankBars);
    }
    setupRankSortControls();
    renderCharts();
    window.addEventListener('load', renderCharts);
  }

  function setupLoginRemember() {
    var form = document.querySelector('[data-login-form]');
    if (!form || !window.localStorage) return;
    var usernameInput = form.querySelector('[data-login-username]');
    var passwordInput = form.querySelector('[data-login-password]');
    var passwordRealInput = form.querySelector('[data-login-password-real]');
    var rememberInput = form.querySelector('[data-login-remember]');
    var storageKey = 'shop.login.remember';
    if (!usernameInput || !passwordInput || !passwordRealInput || !rememberInput) return;
    var maskChar = '*';
    var realPassword = passwordInput.value || '';
    var renderingPassword = false;

    function mask(value) {
      return new Array((value || '').length + 1).join(maskChar);
    }

    function replacePasswordRange(start, end, text) {
      realPassword = realPassword.slice(0, start) + (text || '') + realPassword.slice(end);
      renderPassword(start + (text || '').length);
    }

    function renderPassword(position) {
      renderingPassword = true;
      passwordInput.value = mask(realPassword);
      passwordRealInput.value = realPassword;
      if (document.activeElement === passwordInput && passwordInput.setSelectionRange) {
        passwordInput.setSelectionRange(position, position);
      }
      renderingPassword = false;
    }

    passwordInput.addEventListener('beforeinput', function (event) {
      var start = passwordInput.selectionStart || 0;
      var end = passwordInput.selectionEnd || start;
      var type = event.inputType || '';
      if (type.indexOf('insert') === 0) {
        event.preventDefault();
        var text = type === 'insertFromPaste' && event.clipboardData ? event.clipboardData.getData('text') : event.data;
        replacePasswordRange(start, end, text || '');
      } else if (type === 'deleteContentBackward') {
        event.preventDefault();
        if (start === end && start > 0) start -= 1;
        replacePasswordRange(start, end, '');
      } else if (type === 'deleteContentForward') {
        event.preventDefault();
        if (start === end && end < realPassword.length) end += 1;
        replacePasswordRange(start, end, '');
      } else if (type === 'deleteByCut') {
        event.preventDefault();
        replacePasswordRange(start, end, '');
      }
    });

    passwordInput.addEventListener('paste', function (event) {
      if (!event.clipboardData) return;
      event.preventDefault();
      replacePasswordRange(passwordInput.selectionStart || 0, passwordInput.selectionEnd || 0, event.clipboardData.getData('text'));
    });

    passwordInput.addEventListener('input', function () {
      if (renderingPassword) return;
      if (passwordInput.value && passwordInput.value !== mask(passwordInput.value)) realPassword = passwordInput.value;
      renderPassword(realPassword.length);
    });

    try {
      var saved = JSON.parse(window.localStorage.getItem(storageKey) || 'null');
      if (saved && saved.remember) {
        usernameInput.value = saved.username || usernameInput.value;
        realPassword = saved.password || '';
        renderPassword(realPassword.length);
        rememberInput.checked = true;
      }
    } catch (error) {
      window.localStorage.removeItem(storageKey);
    }

    rememberInput.addEventListener('change', function () {
      if (!rememberInput.checked) window.localStorage.removeItem(storageKey);
    });

    form.addEventListener('submit', function () {
      if (rememberInput.checked) {
        window.localStorage.setItem(storageKey, JSON.stringify({
          remember: true,
          username: usernameInput.value,
          password: realPassword
        }));
      } else {
        window.localStorage.removeItem(storageKey);
      }
      passwordRealInput.value = realPassword;
      renderPassword(realPassword.length);
    });
  }

  setupSearchableGoodsSelects();
  setupDatepickerRange();
  setupSalePlatform();
  setupStockPicker();
  setupMonthPicker();
  setupDashboardCharts();
  setupLoginRemember();
})();
