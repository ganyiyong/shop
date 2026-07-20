(function () {
  function openModal(id, trigger) {
    var modal = document.getElementById(id);
    if (!modal) return;
    modal._returnFocus = trigger || document.activeElement;
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    var heading = modal.querySelector('.modal-head h2');
    if (heading) {
      if (!heading.id) heading.id = id + 'Title';
      if (trigger && trigger.dataset.modalTitle) heading.textContent = trigger.dataset.modalTitle;
      modal.setAttribute('aria-labelledby', heading.id);
    }
    var form = modal.querySelector('form');
    if (form) {
      form.reset();
      var stockLabel = form.querySelector('[data-sale-stock-label]');
      if (stockLabel) {
        stockLabel.textContent = '未选择库存批次';
        var stockControl = stockLabel.closest('.sale-stock-control');
        if (stockControl) stockControl.classList.remove('has-value');
      }
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
    window.setTimeout(function () {
      var focusTarget = modal.querySelector('.search-select-button');
      if (!focusTarget) focusTarget = modal.querySelector('input:not([type="hidden"]):not([aria-hidden="true"]),select:not([aria-hidden="true"]),textarea,button,[href]');
      if (focusTarget) focusTarget.focus();
    }, 0);
  }
  function closeModal(target) {
    var modal = target.closest('.modal');
    if (!modal) return;
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    if (!document.querySelector('.modal.open')) document.body.classList.remove('modal-open');
    if (modal._returnFocus && document.contains(modal._returnFocus)) modal._returnFocus.focus();
  }
  document.querySelectorAll('.modal').forEach(function (modal) {
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    var heading = modal.querySelector('.modal-head h2');
    if (heading) {
      if (!heading.id) heading.id = (modal.id || 'modal') + 'Title';
      modal.setAttribute('aria-labelledby', heading.id);
    }
    modal.querySelectorAll('.icon-btn[data-modal-close]').forEach(function (button) {
      if (!button.getAttribute('aria-label')) button.setAttribute('aria-label', '关闭弹窗');
    });
  });
  document.addEventListener('click', function (event) {
    var button = event.target.closest('[data-modal-open]');
    if (button) openModal(button.getAttribute('data-modal-open'), button);
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
      combo.classList.remove('invalid');
      if (button) button.setAttribute('aria-invalid', 'false');
      var validationError = combo.querySelector('.search-select-error');
      if (validationError) validationError.hidden = true;
      combo.querySelectorAll('[data-search-select-option]').forEach(function (item) {
        var selected = item.dataset.value === select.value;
        item.classList.toggle('selected', selected);
        item.setAttribute('aria-selected', selected ? 'true' : 'false');
      });
    }

    function closeAll(except) {
      document.querySelectorAll('.search-select.open').forEach(function (combo) {
        if (combo !== except) {
          combo.classList.remove('open');
          var comboButton = combo.querySelector('[data-search-select-button]');
          if (comboButton) comboButton.setAttribute('aria-expanded', 'false');
        }
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
      button.setAttribute('aria-expanded', 'false');
      var panel = document.createElement('div');
      panel.className = 'search-select-panel';
      panel.id = 'searchSelectPanel' + Math.random().toString(36).slice(2);
      button.setAttribute('aria-controls', panel.id);
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
      var validationError = document.createElement('small');
      validationError.className = 'search-select-error';
      validationError.id = panel.id + 'Error';
      validationError.textContent = '请选择商品';
      validationError.hidden = true;
      validationError.setAttribute('role', 'alert');
      button.setAttribute('aria-describedby', validationError.id);
      if (select.required) button.setAttribute('aria-required', 'true');

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
          button.setAttribute('aria-expanded', 'false');
          refresh(select);
        });
        list.appendChild(item);
      });

      panel.appendChild(input);
      panel.appendChild(list);
      panel.appendChild(empty);
      combo.appendChild(button);
      combo.appendChild(panel);
      combo.appendChild(validationError);
      select.insertAdjacentElement('afterend', combo);
      select.classList.add('native-search-select');
      select.setAttribute('tabindex', '-1');
      select.setAttribute('aria-hidden', 'true');
      select.addEventListener('invalid', function (event) {
        event.preventDefault();
        combo.classList.add('invalid');
        validationError.hidden = false;
        button.setAttribute('aria-invalid', 'true');
        button.focus();
      });

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
        button.setAttribute('aria-expanded', open ? 'true' : 'false');
        if (open) {
          input.value = '';
          filter();
          alignPanel(combo);
          input.focus();
        }
      });
      input.addEventListener('input', filter);
      input.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
          combo.classList.remove('open');
          button.setAttribute('aria-expanded', 'false');
          button.focus();
        }
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
  var accountMenu = document.querySelector('[data-account-menu]');
  var accountTrigger = accountMenu && accountMenu.querySelector('[data-account-trigger]');
  var accountDropdown = accountMenu && accountMenu.querySelector('[data-account-dropdown]');

  function setAccountMenuOpen(open, restoreFocus) {
    if (!accountMenu || !accountTrigger || !accountDropdown) return;
    accountMenu.classList.toggle('open', open);
    accountTrigger.setAttribute('aria-expanded', open ? 'true' : 'false');
    accountDropdown.hidden = !open;
    if (!open && restoreFocus) accountTrigger.focus();
  }

  if (accountTrigger && accountDropdown) {
    accountTrigger.addEventListener('click', function () {
      setAccountMenuOpen(!accountMenu.classList.contains('open'), false);
    });
    document.addEventListener('click', function (event) {
      if (!accountMenu.contains(event.target)) setAccountMenuOpen(false, false);
    });
  }

  var sidebarCollapseButton = document.querySelector('[data-sidebar-collapse]');
  var sidebarCollapseLabel = sidebarCollapseButton && sidebarCollapseButton.querySelector('[data-sidebar-collapse-label]');

  function syncSidebarCollapseButton() {
    if (!sidebarCollapseButton) return;
    var collapsed = document.documentElement.classList.contains('sidebar-collapsed');
    var label = collapsed ? '展开菜单' : '收起菜单';
    sidebarCollapseButton.setAttribute('aria-expanded', collapsed ? 'false' : 'true');
    sidebarCollapseButton.setAttribute('aria-label', label);
    sidebarCollapseButton.title = label;
    if (sidebarCollapseLabel) sidebarCollapseLabel.textContent = label;
  }

  if (sidebarCollapseButton) {
    sidebarCollapseButton.addEventListener('click', function () {
      if (!window.matchMedia('(min-width: 901px)').matches) return;
      var collapsed = document.documentElement.classList.toggle('sidebar-collapsed');
      try {
        localStorage.setItem('shop.sidebar.collapsed', collapsed ? 'true' : 'false');
      } catch (error) {
        // The current-page toggle still works when storage is unavailable.
      }
      syncSidebarCollapseButton();
    });
    syncSidebarCollapseButton();
  }

  var mobileMenuButton = document.querySelector('[data-mobile-menu]');
  var mobileMoreButton = document.querySelector('[data-mobile-more]');
  var mobileMask = document.querySelector('[data-mobile-mask]');
  var mobileCloseButton = document.querySelector('[data-mobile-close]');
  var mobileSidebar = document.getElementById('mainSidebar');
  var drawerReturnFocus = null;

  function syncMobileMenuAccessibility() {
    if (!mobileSidebar) return;
    var isMobile = window.matchMedia('(max-width: 900px)').matches;
    if (!isMobile) {
      document.body.classList.remove('drawer-open');
      if (mobileMenuButton) mobileMenuButton.setAttribute('aria-expanded', 'false');
      if (mobileMoreButton) mobileMoreButton.setAttribute('aria-expanded', 'false');
    }
    var isOpen = document.body.classList.contains('drawer-open');
    mobileSidebar.inert = isMobile && !isOpen;
    if (isMobile && !isOpen) mobileSidebar.setAttribute('aria-hidden', 'true');
    else mobileSidebar.removeAttribute('aria-hidden');
  }

  function openMobileMenu(trigger) {
    drawerReturnFocus = trigger || document.activeElement;
    document.body.classList.add('drawer-open');
    if (mobileMenuButton) mobileMenuButton.setAttribute('aria-expanded', 'true');
    if (mobileMoreButton) mobileMoreButton.setAttribute('aria-expanded', 'true');
    syncMobileMenuAccessibility();
    window.setTimeout(function () {
      if (mobileCloseButton) mobileCloseButton.focus();
    }, 0);
  }

  function closeMobileMenu() {
    var wasOpen = document.body.classList.contains('drawer-open');
    document.body.classList.remove('drawer-open');
    if (mobileMenuButton) mobileMenuButton.setAttribute('aria-expanded', 'false');
    if (mobileMoreButton) mobileMoreButton.setAttribute('aria-expanded', 'false');
    syncMobileMenuAccessibility();
    if (wasOpen && drawerReturnFocus && document.contains(drawerReturnFocus)) drawerReturnFocus.focus();
  }
  if (mobileMenuButton) {
    mobileMenuButton.addEventListener('click', function () {
      if (document.body.classList.contains('drawer-open')) closeMobileMenu();
      else openMobileMenu(mobileMenuButton);
    });
  }
  if (mobileMoreButton) {
    mobileMoreButton.addEventListener('click', function () {
      openMobileMenu(mobileMoreButton);
    });
  }
  if (mobileMask) mobileMask.addEventListener('click', closeMobileMenu);
  if (mobileCloseButton) mobileCloseButton.addEventListener('click', closeMobileMenu);
  document.querySelectorAll('.sidebar .nav a,.sidebar .logout').forEach(function (link) {
    link.addEventListener('click', closeMobileMenu);
  });
  window.addEventListener('resize', function () {
    syncMobileMenuAccessibility();
    syncSidebarCollapseButton();
    if (window.matchMedia('(max-width: 900px)').matches) setAccountMenuOpen(false, false);
  });
  syncMobileMenuAccessibility();
  document.querySelectorAll('[data-mobile-collapse]').forEach(function (panel) {
    var button = panel.querySelector('[data-mobile-collapse-toggle]');
    var body = panel.querySelector('[data-mobile-collapse-body]');
    var state = button && button.querySelector('[data-mobile-collapse-state]');
    if (!button) return;

    function syncCollapseState() {
      var isMobile = window.matchMedia('(max-width: 900px)').matches;
      var isOpen = panel.classList.contains('open');
      button.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
      if (state) state.textContent = isOpen ? '收起' : '展开';
      if (!body) return;
      if (isMobile) {
        body.setAttribute('aria-hidden', isOpen ? 'false' : 'true');
        body.inert = !isOpen;
        if (!isOpen && body.contains(document.activeElement)) button.focus();
      } else {
        body.removeAttribute('aria-hidden');
        body.inert = false;
      }
    }

    button.addEventListener('click', function () {
      panel.classList.toggle('open');
      syncCollapseState();
    });
    panel.addEventListener('keydown', function (event) {
      if (event.key !== 'Escape' || !panel.classList.contains('open')) return;
      panel.classList.remove('open');
      syncCollapseState();
      button.focus();
    });
    window.addEventListener('resize', syncCollapseState);
    syncCollapseState();
  });
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      var openModals = document.querySelectorAll('.modal.open');
      if (openModals.length) closeModal(openModals[openModals.length - 1]);
      document.querySelectorAll('.date-popover.open').forEach(function (item) { item.classList.remove('open'); });
      setAccountMenuOpen(false, Boolean(accountMenu && accountMenu.classList.contains('open')));
      closeMobileMenu();
    }
    if (event.key === 'Tab') {
      var modals = document.querySelectorAll('.modal.open');
      if (!modals.length) return;
      var activeModal = modals[modals.length - 1];
      var focusable = Array.prototype.slice.call(activeModal.querySelectorAll('a[href],button:not([disabled]),input:not([disabled]):not([type="hidden"]):not([aria-hidden="true"]),select:not([disabled]):not([aria-hidden="true"]):not([tabindex="-1"]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"]):not([aria-hidden="true"])'));
      if (!focusable.length) return;
      var first = focusable[0];
      var last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
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
    function openPickerOnClick(input) {
      input.addEventListener('click', function () {
        if (input.disabled || input.readOnly || typeof input.showPicker !== 'function') return;
        try {
          input.showPicker();
        } catch (error) {
          // Keep the browser's default date-input behavior as the fallback.
        }
      });
    }
    function normalize() {
      if (!startInput.value || !endInput.value) return;
      var start = parseDate(startInput.value);
      var end = parseDate(endInput.value);
      if (start > end) {
        if (document.activeElement === startInput) endInput.value = startInput.value;
        else startInput.value = endInput.value;
      }
    }
    openPickerOnClick(startInput);
    openPickerOnClick(endInput);
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
    var stockControl = stockLabel && stockLabel.closest('.sale-stock-control');
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
      if (stockControl) stockControl.classList.remove('has-value');
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
        if (stockControl) stockControl.classList.add('has-value');
        recalcSale();
        closeModal(button);
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
    var triggers = document.querySelectorAll('[data-month-picker]');
    var popover = document.querySelector('[data-month-popover]');
    if (!triggers.length || !popover) return;
    var title = popover.querySelector('[data-month-title]');
    var grid = popover.querySelector('[data-month-grid]');
    var active = null;
    function readActive(trigger) {
      var form = trigger.closest('form');
      if (!form) return null;
      var key = trigger.getAttribute('data-month-picker') || 'month';
      var valueInput = form.querySelector('[data-month-value="' + key + '"]') || form.querySelector('[data-month-value]');
      var label = trigger.querySelector('[data-month-label]') || form.querySelector('[data-month-label="' + key + '"]') || form.querySelector('[data-month-label]');
      if (!valueInput) return null;
      var selected = valueInput.value || fmt(new Date()).slice(0, 7);
      return {
        key: key,
        trigger: trigger,
        form: form,
        valueInput: valueInput,
        label: label,
        selected: selected,
        originalSelected: selected,
        year: Number(selected.slice(0, 4)) || new Date().getFullYear()
      };
    }
    function monthLabel(value) {
      return Number(value.slice(0, 4)) + '年 ' + Number(value.slice(5, 7)) + '月';
    }
    function previousMonthValue(value) {
      var parts = (value || '').split('-').map(Number);
      var date = parts.length === 2 ? new Date(parts[0], parts[1] - 2, 1) : new Date();
      return date.getFullYear() + '-' + pad(date.getMonth() + 1);
    }
    function renderMonth() {
      if (!active) return;
      title.textContent = active.year + '年';
      var html = '';
      for (var i = 1; i <= 12; i++) {
        var value = active.year + '-' + pad(i);
        html += '<button type="button" class="' + (value === active.selected ? 'selected' : '') + '" data-month-option="' + value + '">' + i + '月</button>';
      }
      grid.innerHTML = html;
      active.valueInput.value = active.selected;
      if (active.label) active.label.textContent = monthLabel(active.selected);
    }
    function submitMonth() {
      if (!active) return;
      active.valueInput.value = active.selected;
      if (active.key === 'month') {
        var compareInput = active.form.querySelector('[data-month-value="compareMonth"]');
        var previousDefault = previousMonthValue(active.originalSelected);
        if (compareInput && (!compareInput.value || compareInput.value === previousDefault)) {
          compareInput.value = previousMonthValue(active.selected);
        }
      }
      popover.classList.remove('open');
      active.form.submit();
    }
    triggers.forEach(function (trigger) {
      trigger.addEventListener('click', function () {
        var nextActive = readActive(trigger);
        if (!nextActive) return;
        var wasOpen = popover.classList.contains('open') && active && active.trigger === trigger;
        active = nextActive;
        if (wasOpen) {
          popover.classList.remove('open');
          return;
        }
        var rect = trigger.getBoundingClientRect();
        popover.style.left = Math.max(window.scrollX + 12, rect.left + window.scrollX) + 'px';
        popover.style.top = rect.bottom + window.scrollY + 8 + 'px';
        popover.classList.add('open');
        renderMonth();
        var maxLeft = window.scrollX + window.innerWidth - popover.offsetWidth - 12;
        popover.style.left = Math.max(window.scrollX + 12, Math.min(rect.left + window.scrollX, maxLeft)) + 'px';
      });
    });
    popover.addEventListener('click', function (event) {
      if (!active) return;
      var option = event.target.closest('[data-month-option]');
      if (option) {
        active.selected = option.dataset.monthOption;
        active.year = Number(active.selected.slice(0, 4));
        renderMonth();
        submitMonth();
      }
      if (event.target.matches('[data-month-prev]')) { active.year -= 1; renderMonth(); }
      if (event.target.matches('[data-month-next]')) { active.year += 1; renderMonth(); }
      if (event.target.matches('[data-month-now]')) {
        active.selected = fmt(new Date()).slice(0, 7);
        active.year = Number(active.selected.slice(0, 4));
        submitMonth();
      }
      if (event.target.matches('[data-month-apply]')) submitMonth();
    });
    document.addEventListener('click', function (event) {
      if (!popover.contains(event.target) && !event.target.closest('[data-month-picker]')) popover.classList.remove('open');
    });
  }

  function setupAssetSnapshotSave() {
    var button = document.querySelector('[data-asset-snapshot-save]');
    if (!button || !window.fetch) return;
    var form = button.closest('form');
    var status = form && form.querySelector('[data-asset-snapshot-status]');
    if (!form) return;
    var defaultText = button.textContent;

    function showStatus(message, isError) {
      if (!status) return;
      status.hidden = false;
      status.textContent = message;
      status.classList.toggle('error', !!isError);
    }

    button.addEventListener('click', function () {
      if (!form.checkValidity()) {
        form.reportValidity();
        return;
      }
      var url = button.dataset.url || form.action;
      button.disabled = true;
      button.classList.add('is-loading');
      button.textContent = '保存中';
      if (status) status.hidden = true;

      fetch(url, {
        method: 'POST',
        body: new FormData(form),
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      }).then(function (response) {
        if (!response.ok) throw new Error('request failed');
        return response.json();
      }).then(function (data) {
        showStatus(data.message || '快照保存成功', false);
      }).catch(function () {
        showStatus('快照保存失败，请稍后重试', true);
      }).finally(function () {
        button.disabled = false;
        button.classList.remove('is-loading');
        button.textContent = defaultText;
      });
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
      if (!points.length) {
        container.innerHTML = '<div class="chart-empty">暂无库存数据</div>';
        return;
      }
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

  function setupResponsiveDetails() {
    var mobile = window.matchMedia('(max-width: 900px)').matches;
    document.querySelectorAll('[data-responsive-details]').forEach(function (details) {
      details.open = !mobile;
    });
  }

  function setupLoginRemember() {
    var form = document.querySelector('[data-login-form]');
    if (!form || !window.localStorage) return;
    var usernameInput = form.querySelector('[data-login-username]');
    var rememberInput = form.querySelector('[data-login-remember]');
    var storageKey = 'shop.login.remember';
    if (!usernameInput || !rememberInput) return;

    try {
      var saved = JSON.parse(window.localStorage.getItem(storageKey) || 'null');
      if (saved && saved.remember) {
        usernameInput.value = saved.username || usernameInput.value;
        rememberInput.checked = true;
        window.localStorage.setItem(storageKey, JSON.stringify({ remember: true, username: usernameInput.value }));
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
          username: usernameInput.value
        }));
      } else {
        window.localStorage.removeItem(storageKey);
      }
    });
  }

  function setupPartSaleCalculator() {
    var form = document.querySelector('[data-part-sale-form]');
    if (!form) return;
    var partNumbersInput = form.querySelector('[data-part-numbers]');
    var sellingInput = form.querySelector('[data-part-selling]');
    var costInput = form.querySelector('[data-part-cost]');
    var profitInput = form.querySelector('[data-part-profit]');
    if (!partNumbersInput || !sellingInput || !costInput || !profitInput) return;

    function number(value) {
      var parsed = Number(value || 0);
      return Number.isFinite(parsed) ? parsed : 0;
    }

    function recalculate() {
      var partCount = partNumbersInput.value.split(/[,，]/).filter(function (value) {
        return value.trim().length > 0;
      }).length || 1;
      var sellingPrice = number(sellingInput.value) / partCount;
      var cost = number(costInput.value) / partCount;
      var profit = sellingPrice - cost;
      profitInput.value = (Math.round(profit * 100) / 100).toFixed(2);
      profitInput.classList.toggle('text-bad', profit < 0);
      profitInput.classList.toggle('text-good', profit >= 0);
    }

    partNumbersInput.addEventListener('input', recalculate);
    sellingInput.addEventListener('input', recalculate);
    costInput.addEventListener('input', recalculate);
    form.addEventListener('submit', recalculate);
    document.addEventListener('click', function (event) {
      if (event.target.closest('[data-modal-open="partSaleModal"]')) {
        window.setTimeout(recalculate, 0);
      }
    });
    recalculate();
  }

  function setupPartBatchLoader() {
    var workspace = document.querySelector('.part-workspace');
    if (!workspace || !window.fetch) return;
    var controller = null;

    function matchingLink(url) {
      var batchId = new URL(url, window.location.href).searchParams.get('batchId');
      return Array.prototype.find.call(document.querySelectorAll('[data-part-batch-link]'), function (link) {
        return new URL(link.href, window.location.href).searchParams.get('batchId') === batchId;
      });
    }

    function setActive(link) {
      document.querySelectorAll('[data-part-batch-link]').forEach(function (item) {
        var active = item === link;
        item.classList.toggle('active', active);
        if (active) item.setAttribute('aria-current', 'true');
        else item.removeAttribute('aria-current');
      });
    }

    function load(link, updateHistory) {
      var currentDetail = workspace.querySelector('[data-part-detail]');
      if (!currentDetail || !link.dataset.detailUrl) {
        window.location.assign(link.href);
        return;
      }
      if (controller) controller.abort();
      controller = new AbortController();
      var requestController = controller;
      currentDetail.classList.add('is-loading');
      currentDetail.setAttribute('aria-busy', 'true');
      setActive(link);

      fetch(link.dataset.detailUrl, {
        signal: requestController.signal,
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      }).then(function (response) {
        if (!response.ok) throw new Error('Part detail request failed');
        return response.text();
      }).then(function (html) {
        if (requestController !== controller) return;
        var template = document.createElement('template');
        template.innerHTML = html.trim();
        var nextDetail = template.content.querySelector('[data-part-detail]');
        if (!nextDetail) throw new Error('Part detail response is invalid');
        currentDetail.replaceWith(nextDetail);
        if (updateHistory) window.history.pushState({ partBatch: true }, '', link.href);
      }).catch(function (error) {
        if (error.name === 'AbortError') return;
        window.location.assign(link.href);
      }).finally(function () {
        if (requestController !== controller) return;
        controller = null;
        var detail = workspace.querySelector('[data-part-detail]');
        if (detail) {
          detail.classList.remove('is-loading');
          detail.removeAttribute('aria-busy');
        }
      });
    }

    workspace.addEventListener('click', function (event) {
      var link = event.target.closest('[data-part-batch-link]');
      if (!link || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      event.preventDefault();
      load(link, true);
    });

    window.addEventListener('popstate', function () {
      var link = matchingLink(window.location.href);
      if (link) load(link, false);
      else window.location.reload();
    });
  }

  setupSearchableGoodsSelects();
  setupDatepickerRange();
  setupSalePlatform();
  setupStockPicker();
  setupMonthPicker();
  setupAssetSnapshotSave();
  setupResponsiveDetails();
  setupDashboardCharts();
  setupLoginRemember();
  setupPartSaleCalculator();
  setupPartBatchLoader();
})();
