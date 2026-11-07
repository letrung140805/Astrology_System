(() => {
    const showToast = (message) => {
        const current = document.querySelector('.toast');
        if (current) current.remove();
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.setAttribute('role', 'status');
        toast.textContent = message;
        document.body.append(toast);
        window.setTimeout(() => toast.remove(), 3200);
    };

    if (window.localStorage.getItem('tuvi-theme') === 'dark') {
        document.body.classList.add('dark-mode');
    }

    document.querySelectorAll('.theme-toggle').forEach((button) => {
        button.addEventListener('click', () => {
            const enabled = document.body.classList.toggle('dark-mode');
            window.localStorage.setItem('tuvi-theme', enabled ? 'dark' : 'light');
        });
    });

    document.querySelectorAll('.profile-button').forEach((button) => {
        button.addEventListener('click', () => {
            const wrap = button.closest('.profile-wrap');
            const expanded = wrap.classList.toggle('open');
            button.setAttribute('aria-expanded', String(expanded));
        });
    });

    // Đồng bộ trạng thái trang hiện tại trên thanh điều hướng chính
    const currentNavPath = window.location.pathname;

    // Tự động chuyển hướng nếu vừa đăng nhập xong từ một trang yêu cầu xác thực
    try {
        const targetRedirect = sessionStorage.getItem('tuvi_auth_redirect');
        if (targetRedirect && currentNavPath === '/' && targetRedirect !== '/') {
            sessionStorage.removeItem('tuvi_auth_redirect');
            window.location.href = targetRedirect;
            return;
        }
    } catch (e) {}

    const mainNavLinks = document.querySelectorAll('.main-nav a');
    if (mainNavLinks.length > 0) {
        mainNavLinks.forEach((link) => {
            const href = link.getAttribute('href');
            if (href === currentNavPath || (href !== '/' && currentNavPath.startsWith(href)) || (href === '/history' && currentNavPath.startsWith('/result'))) {
                link.classList.add('active');
            } else if (href === '/' && currentNavPath !== '/') {
                link.classList.remove('active');
            }
        });
    }

    document.querySelectorAll('.mobile-menu').forEach((button) => {
        button.addEventListener('click', () => {
            const nav = document.querySelector('.main-nav');
            const expanded = nav.classList.toggle('open');
            button.setAttribute('aria-expanded', String(expanded));
        });
    });

    document.querySelectorAll('.language-switch button').forEach((button) => {
        button.addEventListener('click', () => {
            document.querySelectorAll('.language-switch button').forEach((item) => item.classList.remove('selected'));
            button.classList.add('selected');
            if (button.textContent.trim() === 'EN') showToast('Bản tiếng Anh sẽ sớm ra mắt.');
        });
    });

    const updateGenderChoiceVisuals = (groupName) => {
        document.querySelectorAll(`.gender-choice input[name="${groupName}"]`).forEach((r) => {
            const choice = r.closest('.gender-choice');
            if (choice) {
                choice.classList.toggle('selected', r.checked);
            }
        });
    };

    document.querySelectorAll('.gender-choice input[type="radio"]').forEach((radio) => {
        radio.addEventListener('change', () => {
            updateGenderChoiceVisuals(radio.name);
        });
    });

    document.querySelectorAll('.gender-choice').forEach((choice) => {
        choice.addEventListener('click', () => {
            const radio = choice.querySelector('input[type="radio"]');
            if (radio && !radio.checked) {
                radio.checked = true;
                updateGenderChoiceVisuals(radio.name);
                radio.dispatchEvent(new Event('change', { bubbles: true }));
            }
        });
    });

    // Helper for birth date (day, month, year) synchronization
    const initDateSync = (dayId, monthId, yearId, hiddenId) => {
        const daySelect = document.getElementById(dayId);
        const monthSelect = document.getElementById(monthId);
        const yearSelect = document.getElementById(yearId);
        const hiddenDate = document.getElementById(hiddenId);

        if (!daySelect || !monthSelect || !yearSelect || !hiddenDate) return null;

        const update = () => {
            const day = parseInt(daySelect.value, 10);
            const month = parseInt(monthSelect.value, 10);
            const year = parseInt(yearSelect.value, 10) || 2024;

            if (month) {
                const maxDays = new Date(year, month, 0).getDate();
                Array.from(daySelect.options).forEach((opt) => {
                    const val = parseInt(opt.value, 10);
                    if (val) {
                        const isValid = val <= maxDays;
                        opt.hidden = !isValid;
                        opt.disabled = !isValid;
                    }
                });
                if (day > maxDays) daySelect.value = '';
            }

            const cDay = daySelect.value;
            const cMonth = monthSelect.value;
            const cYear = yearSelect.value;
            if (cDay && cMonth && cYear) {
                hiddenDate.value = `${cYear}-${String(cMonth).padStart(2, '0')}-${String(cDay).padStart(2, '0')}`;
            } else {
                hiddenDate.value = '';
            }
        };

        daySelect.addEventListener('change', update);
        monthSelect.addEventListener('change', update);
        yearSelect.addEventListener('change', update);
        update();
        return update;
    };

    const updateDaysAndSyncDate = initDateSync('birthDay', 'birthMonth', 'birthYear', 'birthDate');
    const syncCouple1 = initDateSync('birthDay1', 'birthMonth1', 'birthYear1', 'birthDate1');
    const syncCouple2 = initDateSync('birthDay2', 'birthMonth2', 'birthYear2', 'birthDate2');

    const coupleForm = document.getElementById('coupleForm');
    if (coupleForm) {
        try {
            const savedCouple = sessionStorage.getItem('tuvi_pending_couple_chart');
            if (savedCouple) {
                const data = JSON.parse(savedCouple);
                if (data.name1 && document.getElementById('fullName1')) document.getElementById('fullName1').value = data.name1;
                if (data.gender1) {
                    const r1 = document.querySelector('input[name="gender1"][value="' + data.gender1 + '"]');
                    if (r1) {
                        r1.checked = true;
                        updateGenderChoiceVisuals('gender1');
                    }
                }
                const bDay1 = document.getElementById('birthDay1');
                const bMonth1 = document.getElementById('birthMonth1');
                const bYear1 = document.getElementById('birthYear1');
                if (data.birthDay1 && bDay1) bDay1.value = data.birthDay1;
                if (data.birthMonth1 && bMonth1) bMonth1.value = data.birthMonth1;
                if (data.birthYear1 && bYear1) bYear1.value = data.birthYear1;
                if (data.birthTime1 && document.getElementById('birthTime1')) document.getElementById('birthTime1').value = data.birthTime1;
                if (data.birthplace1 && document.getElementById('birthplace1')) document.getElementById('birthplace1').value = data.birthplace1;

                if (data.name2 && document.getElementById('fullName2')) document.getElementById('fullName2').value = data.name2;
                if (data.gender2) {
                    const r2 = document.querySelector('input[name="gender2"][value="' + data.gender2 + '"]');
                    if (r2) {
                        r2.checked = true;
                        updateGenderChoiceVisuals('gender2');
                    }
                }
                const bDay2 = document.getElementById('birthDay2');
                const bMonth2 = document.getElementById('birthMonth2');
                const bYear2 = document.getElementById('birthYear2');
                if (data.birthDay2 && bDay2) bDay2.value = data.birthDay2;
                if (data.birthMonth2 && bMonth2) bMonth2.value = data.birthMonth2;
                if (data.birthYear2 && bYear2) bYear2.value = data.birthYear2;
                if (data.birthTime2 && document.getElementById('birthTime2')) document.getElementById('birthTime2').value = data.birthTime2;
                if (data.birthplace2 && document.getElementById('birthplace2')) document.getElementById('birthplace2').value = data.birthplace2;

                if (syncCouple1) syncCouple1();
                if (syncCouple2) syncCouple2();
                sessionStorage.removeItem('tuvi_pending_couple_chart');
            }
        } catch (e) {}

        coupleForm.addEventListener('submit', (e) => {
            if (syncCouple1) syncCouple1();
            if (syncCouple2) syncCouple2();

            if (coupleForm.dataset.authenticated === 'false') {
                e.preventDefault();
                try {
                    const bDay1 = document.getElementById('birthDay1');
                    const bMonth1 = document.getElementById('birthMonth1');
                    const bYear1 = document.getElementById('birthYear1');
                    const bDay2 = document.getElementById('birthDay2');
                    const bMonth2 = document.getElementById('birthMonth2');
                    const bYear2 = document.getElementById('birthYear2');
                    const data = {
                        name1: document.getElementById('fullName1') ? document.getElementById('fullName1').value : '',
                        gender1: document.querySelector('input[name="gender1"]:checked') ? document.querySelector('input[name="gender1"]:checked').value : 'Nam',
                        birthDay1: bDay1 ? bDay1.value : '',
                        birthMonth1: bMonth1 ? bMonth1.value : '',
                        birthYear1: bYear1 ? bYear1.value : '',
                        birthTime1: document.getElementById('birthTime1') ? document.getElementById('birthTime1').value : '',
                        birthplace1: document.getElementById('birthplace1') ? document.getElementById('birthplace1').value : '',

                        name2: document.getElementById('fullName2') ? document.getElementById('fullName2').value : '',
                        gender2: document.querySelector('input[name="gender2"]:checked') ? document.querySelector('input[name="gender2"]:checked').value : 'Nữ',
                        birthDay2: bDay2 ? bDay2.value : '',
                        birthMonth2: bMonth2 ? bMonth2.value : '',
                        birthYear2: bYear2 ? bYear2.value : '',
                        birthTime2: document.getElementById('birthTime2') ? document.getElementById('birthTime2').value : '',
                        birthplace2: document.getElementById('birthplace2') ? document.getElementById('birthplace2').value : ''
                    };
                    sessionStorage.setItem('tuvi_pending_couple_chart', JSON.stringify(data));
                    sessionStorage.setItem('tuvi_auth_redirect', '/tuong-hop');
                } catch (err) {}
                window.location.href = '/login?required=true';
            }
        });
    }

    const editCoupleBtn = document.getElementById('editCoupleBtn');
    if (editCoupleBtn) {
        editCoupleBtn.addEventListener('click', () => {
            const section = document.getElementById('coupleFormSection');
            if (section) {
                section.classList.remove('form-collapsed');
                section.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }

    const scrollToAnalysisBtn = document.getElementById('scrollToAnalysisBtn');
    if (scrollToAnalysisBtn) {
        scrollToAnalysisBtn.addEventListener('click', () => {
            const el = document.getElementById('coupleAnalysisSection');
            if (el) el.scrollIntoView({ behavior: 'smooth' });
        });
    }

    const downloadCouplePdfBtn = document.getElementById('downloadCouplePdfBtn');
    if (downloadCouplePdfBtn) {
        downloadCouplePdfBtn.addEventListener('click', () => {
            if (typeof html2pdf === 'undefined') {
                alert('Đang nạp công cụ tạo PDF, vui lòng thử lại sau giây lát.');
                return;
            }
            const frame = document.getElementById('coupleChartsFrame');
            if (!frame) return;
            const opt = {
                margin: [8, 6, 8, 6],
                filename: 'la-so-tu-vi-cap-doi.pdf',
                image: { type: 'jpeg', quality: 0.98 },
                html2canvas: { scale: 1.8, useCORS: true },
                jsPDF: { unit: 'mm', format: 'a4', orientation: 'landscape' }
            };
            html2pdf().set(opt).from(frame).save();
        });
    }

        const chartForm = document.querySelector('.chart-form');
        if (chartForm) {
            try {
                const saved = sessionStorage.getItem('tuvi_pending_chart');
                if (saved) {
                    const data = JSON.parse(saved);
                    if (data.name && document.getElementById('fullName')) document.getElementById('fullName').value = data.name;
                    if (data.gender) {
                        const radio = document.querySelector('input[name="gender"][value="' + data.gender + '"]');
                        if (radio) {
                            radio.checked = true;
                            updateGenderChoiceVisuals('gender');
                        }
                    }
                    if (data.birthDay && birthDaySelect) birthDaySelect.value = data.birthDay;
                    if (data.birthMonth && birthMonthSelect) birthMonthSelect.value = data.birthMonth;
                    if (data.birthYear && birthYearSelect) birthYearSelect.value = data.birthYear;
                    if (data.birthTime && document.getElementById('birthTime')) document.getElementById('birthTime').value = data.birthTime;
                    if (data.birthplace && document.getElementById('birthplace')) document.getElementById('birthplace').value = data.birthplace;
                    updateDaysAndSyncDate();
                    sessionStorage.removeItem('tuvi_pending_chart');
                }
            } catch (e) {}

            chartForm.addEventListener('submit', (e) => {
                updateDaysAndSyncDate();
                if (chartForm.dataset.authenticated === 'false') {
                    e.preventDefault();
                    try {
                        const data = {
                            name: document.getElementById('fullName') ? document.getElementById('fullName').value : '',
                            gender: document.querySelector('input[name="gender"]:checked') ? document.querySelector('input[name="gender"]:checked').value : 'Nữ',
                            birthDay: birthDaySelect ? birthDaySelect.value : '',
                            birthMonth: birthMonthSelect ? birthMonthSelect.value : '',
                            birthYear: birthYearSelect ? birthYearSelect.value : '',
                            birthTime: document.getElementById('birthTime') ? document.getElementById('birthTime').value : '',
                            birthplace: document.getElementById('birthplace') ? document.getElementById('birthplace').value : ''
                        };
                        sessionStorage.setItem('tuvi_pending_chart', JSON.stringify(data));
                    } catch (err) {}
                    window.location.href = '/login?required=true';
                }
            });
        }

    document.querySelectorAll('[data-toast]').forEach((button) => {
        button.addEventListener('click', (event) => {
            if (button.tagName === 'A') event.preventDefault();
            showToast(button.dataset.toast);
        });
    });

    document.querySelectorAll('[data-print]').forEach((button) => {
        button.addEventListener('click', () => window.print());
    });

    const downloadPdfBtn = document.getElementById('downloadPdfBtn');
    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener('click', downloadChartPdf);
    }

    function downloadChartPdf() {
        const btn = document.getElementById('downloadPdfBtn');
        if (!btn) return;

        if (typeof html2pdf === 'undefined') {
            alert('Đang nạp công cụ tạo PDF, vui lòng thử lại sau giây lát.');
            return;
        }

        const originalHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<svg class="outline-icon spin" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-6.219-8.56"></path></svg> <span>Đang tạo PDF...</span>';

        const chartWrapper = document.getElementById('astrologyChartOnly');
        if (!chartWrapper) {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
            alert('Không tìm thấy khung lá số để tải.');
            return;
        }

        // Full-screen loading overlay
        const overlay = document.createElement('div');
        overlay.id = 'pdfLoadingOverlay';
        overlay.style.cssText = 'position:fixed;top:0;left:0;width:100vw;height:100vh;background:rgba(20,18,38,0.7);backdrop-filter:blur(5px);z-index:999999;display:flex;align-items:center;justify-content:center;color:#191943;';
        overlay.innerHTML = `
            <div style="background:#ffffff;padding:28px 36px;border-radius:18px;box-shadow:0 20px 48px rgba(0,0,0,0.3);display:flex;flex-direction:column;align-items:center;gap:12px;text-align:center;max-width:90vw;">
                <svg class="outline-icon spin" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#6158a8" stroke-width="2.5"><path d="M21 12a9 9 0 1 1-6.219-8.56"></path></svg>
                <div style="font-weight:700;font-size:17px;font-family:'Times New Roman',Times,Georgia,serif;color:#191943;">Đang xuất lá số PDF độ nét cao...</div>
                <div style="font-size:13px;color:#686776;">Hệ thống đang chuẩn hóa cấu trúc 12 cung và ngũ hành. Vui lòng đợi trong giây lát...</div>
            </div>
        `;
        document.body.appendChild(overlay);

        // Create an unconstrained sandbox container attached directly to body
        // Matching A4 printable width (~198mm = 748px) to prevent any clipping
        const tempContainer = document.createElement('div');
        tempContainer.id = 'pdfCaptureSandbox';
        tempContainer.style.cssText = 'position:fixed;top:0;left:0;width:748px;max-width:748px;background:#ffffff;z-index:999990;overflow:visible;box-sizing:border-box;pointer-events:none;';

        const clone = chartWrapper.cloneNode(true);
        clone.classList.add('pdf-chart-capture');
        tempContainer.appendChild(clone);
        document.body.appendChild(tempContainer);

        const isDark = document.body.classList.contains('dark-mode');
        if (isDark) {
            document.body.classList.remove('dark-mode');
        }

        const nameEl = document.querySelector('.result-titlebar em') || document.querySelector('.center-name-val');
        const rawName = nameEl ? nameEl.textContent.trim() : 'nguoi-dung';
        const cleanName = rawName.toLowerCase()
            .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd').replace(/Đ/g, 'D')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-+|-+$/g, '') || 'lasotuvi';
        const filename = `la-so-tu-vi-${cleanName}.pdf`;

        const opt = {
            margin: [8, 6, 8, 6],
            filename: filename,
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: {
                scale: 2,
                useCORS: true,
                logging: false,
                scrollX: 0,
                scrollY: 0,
                backgroundColor: '#ffffff'
            },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };

        html2pdf().set(opt).from(clone).save()
            .then(() => {
                showToast('Đã tải lá số PDF thành công!');
            })
            .catch((err) => {
                console.error('Lỗi xuất PDF:', err);
                showToast('Đã xảy ra lỗi khi tạo file PDF. Vui lòng thử lại.');
            })
            .finally(() => {
                btn.disabled = false;
                btn.innerHTML = originalHtml;
                if (overlay && overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
                if (tempContainer && tempContainer.parentNode) {
                    tempContainer.parentNode.removeChild(tempContainer);
                }
                if (isDark) {
                    document.body.classList.add('dark-mode');
                }
            });
    }

    document.querySelectorAll('[data-share]').forEach((button) => {
        button.addEventListener('click', () => {
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(window.location.href)
                    .then(() => showToast('Đã sao chép liên kết lá số.'))
                    .catch(() => showToast('Không thể sao chép liên kết. Hãy thử lại trên kết nối bảo mật.'));
            } else {
                showToast('Sao chép liên kết từ thanh địa chỉ trình duyệt để chia sẻ lá số.');
            }
        });
    });

    document.querySelectorAll('.password-toggle').forEach((button) => {
        button.addEventListener('click', () => {
            const input = button.closest('.input-with-icon').querySelector('input');
            if (!input) return;
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            const eyeOpen = button.querySelector('.eye-open');
            const eyeClosed = button.querySelector('.eye-closed');
            if (eyeOpen && eyeClosed) {
                eyeOpen.classList.toggle('hidden', !isPassword);
                eyeClosed.classList.toggle('hidden', isPassword);
            }
        });
    });

    document.querySelectorAll('[data-auth-tab]').forEach((button) => {
        button.addEventListener('click', () => {
            document.querySelectorAll('[data-auth-tab]').forEach((tab) => tab.classList.toggle('selected', tab === button));
            const isRegister = button.dataset.authTab === 'register';
            const loginForm = document.getElementById('loginForm');
            const registerForm = document.getElementById('registerForm');
            if (loginForm && registerForm) {
                loginForm.style.display = isRegister ? 'none' : 'block';
                registerForm.style.display = isRegister ? 'block' : 'none';
            }
        });
    });



    document.querySelectorAll('.account-form').forEach((form) => {
        form.addEventListener('submit', (event) => {
            event.preventDefault();
            showToast('Thông tin đã được cập nhật trong bản xem trước.');
        });
    });

    document.querySelectorAll('[data-palace]').forEach((palace) => {
        palace.addEventListener('click', () => {
            document.querySelectorAll('[data-palace]').forEach((item) => item.classList.toggle('selected', item === palace));
            const name = palace.dataset.palace;
            const nameTarget = document.querySelector('[data-palace-name]');
            const description = document.querySelector('[data-palace-description]');
            if (nameTarget) nameTarget.textContent = name;
            if (description) description.textContent = `Cung ${name} được luận giải dựa trên chính tinh, phụ tinh và vận hạn đang hội chiếu. Hãy xem thêm các trụ vận mệnh bên dưới để tham khảo góc nhìn tổng quan.`;
        });
    });

    document.querySelectorAll('[data-expand-all]').forEach((button) => {
        button.addEventListener('click', () => {
            const cards = Array.from(document.querySelectorAll('.reading-card'));
            const shouldOpen = cards.some((card) => !card.open);
            cards.forEach((card) => { card.open = shouldOpen; });
            button.textContent = shouldOpen ? '⌄ Thu gọn toàn bộ' : '☷ Mở rộng toàn bộ';
        });
    });

    document.querySelectorAll('[data-account-tab]').forEach((button) => {
        button.addEventListener('click', () => {
            const tab = button.dataset.accountTab;
            document.querySelectorAll('[data-account-tab]').forEach((item) => item.classList.toggle('active', item === button));
            document.querySelectorAll('[data-account-panel]').forEach((panel) => panel.classList.toggle('hidden', panel.dataset.accountPanel !== tab));
        });
    });

    // AI Chat Interaction & Real-time Streaming
    const chatForm = document.querySelector('[data-chat-form]');
    const chatMessages = document.querySelector('[data-chat-messages]');

    const renderMarkdown = (text) => {
        if (!text) return '';
        if (window.marked && typeof window.marked.parse === 'function') {
            try {
                return window.marked.parse(text, { breaks: true, gfm: true });
            } catch (e) {
                console.warn('Marked parse error', e);
            }
        }
        const escaped = text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        return escaped
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/\n/g, '<br>');
    };

    if (chatMessages) {
        document.querySelectorAll('.message.assistant .message-bubble').forEach((bubble) => {
            const raw = bubble.dataset.rawContent || bubble.textContent;
            if (raw && raw.trim()) {
                bubble.innerHTML = renderMarkdown(raw.trim());
            }
        });
    }

    if (chatForm && chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;

        const chatInput = chatForm.querySelector('input[name="message"]');
        const sendButton = chatForm.querySelector('button[type="submit"]');
        const convInput = chatForm.querySelector('input[name="conversationId"]');
        const csrfInput = chatForm.querySelector('input[name="_csrf"]');
        const streamUrl = chatForm.dataset.streamUrl || (chatForm.action + '/stream');

        const applySuggestion = (text) => {
            if (!chatInput || !text) return;
            chatInput.value = text;
            chatInput.disabled = false;
            chatInput.focus();
            const len = chatInput.value.length;
            try {
                chatInput.setSelectionRange(len, len);
            } catch (e) {}
            chatInput.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        };

        const updateFollowUpSuggestions = (suggestions) => {
            const list = document.querySelector('.quick-prompts-list');
            if (!list || !Array.isArray(suggestions) || suggestions.length === 0) return;
            list.innerHTML = '';
            suggestions.slice(0, 3).forEach((text) => {
                if (!text || typeof text !== 'string') return;
                const clean = text.trim();
                if (!clean) return;
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.setAttribute('data-prompt', clean);
                btn.innerHTML = `<span>${escapeHtml(clean)}</span>`;
                btn.addEventListener('click', () => applySuggestion(clean));
                list.appendChild(btn);
            });
        };

        document.querySelectorAll('.quick-prompts-list [data-prompt]').forEach((button) => {
            button.addEventListener('click', () => {
                const promptText = button.getAttribute('data-prompt');
                if (promptText) {
                    applySuggestion(promptText);
                }
            });
        });

        chatForm.addEventListener('submit', async (event) => {
            const message = chatInput ? chatInput.value.trim() : '';
            if (!message) return;

            if (!window.ReadableStream || !window.fetch) {
                return;
            }

            event.preventDefault();

            chatInput.value = '';
            chatInput.disabled = true;
            if (sendButton) sendButton.disabled = true;

            const userInitial = (chatMessages.dataset.userInitial || document.querySelector('.authenticated-avatar-btn .avatar-initial-circle')?.textContent || 'B').trim() || 'B';
            const userArticle = document.createElement('article');
            userArticle.className = 'message user-message';
            userArticle.innerHTML = `
                <div>
                    <div class="message-bubble"></div>
                    <small>Bạn</small>
                </div>
                <span class="chat-avatar chat-avatar-user">${escapeHtml(userInitial)}</span>
            `;
            userArticle.querySelector('.message-bubble').textContent = message;
            chatMessages.appendChild(userArticle);

            const aiArticle = document.createElement('article');
            aiArticle.className = 'message assistant';
            aiArticle.innerHTML = `
                <img class="chat-avatar chat-avatar-ai" src="/images/ai-avatar.jpg" alt="Tử Vi AI" />
                <div>
                    <div class="message-bubble"><div class="bubble-content"></div><span class="typing-cursor"></span></div>
                    <small>Tử Vi AI</small>
                </div>
            `;
            chatMessages.appendChild(aiArticle);
            chatMessages.scrollTop = chatMessages.scrollHeight;

            const bubbleContent = aiArticle.querySelector('.bubble-content');
            const bubbleEl = aiArticle.querySelector('.message-bubble');
            const cursor = aiArticle.querySelector('.typing-cursor');
            let accumulatedMarkdown = '';

            const params = new URLSearchParams();
            params.append('message', message);
            if (convInput && convInput.value) {
                params.append('conversationId', convInput.value);
            }
            if (csrfInput && csrfInput.value) {
                params.append('_csrf', csrfInput.value);
            }

            try {
                const response = await fetch(streamUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Accept': 'text/event-stream'
                    },
                    body: params.toString()
                });

                if (!response.ok) {
                    throw new Error(`Máy chủ phản hồi mã lỗi ${response.status}`);
                }

                const reader = response.body.getReader();
                const decoder = new TextDecoder('utf-8');
                let buffer = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split('\n');
                    buffer = lines.pop();

                    for (const line of lines) {
                        const trimmed = line.trim();
                        if (!trimmed.startsWith('data:')) continue;

                        const dataStr = trimmed.substring(5).trim();
                        if (!dataStr) continue;

                        try {
                            const data = JSON.parse(dataStr);
                            if (data.token) {
                                accumulatedMarkdown += data.token;
                                bubbleContent.innerHTML = renderMarkdown(accumulatedMarkdown);
                                chatMessages.scrollTop = chatMessages.scrollHeight;
                            }
                            if (data.error) {
                                if (accumulatedMarkdown) {
                                    accumulatedMarkdown += `\n\n> ⚠️ *${escapeHtml(data.error)}*`;
                                    bubbleContent.innerHTML = renderMarkdown(accumulatedMarkdown);
                                } else {
                                    bubbleEl.classList.add('error-bubble');
                                    bubbleContent.textContent = data.error;
                                }
                            }
                            if (data.conversationId) {
                                let cInput = chatForm.querySelector('input[name="conversationId"]');
                                if (!cInput) {
                                    cInput = document.createElement('input');
                                    cInput.type = 'hidden';
                                    cInput.name = 'conversationId';
                                    cInput.id = 'chatConversationId';
                                    chatForm.appendChild(cInput);
                                }
                                cInput.value = data.conversationId;
                                const url = new URL(window.location);
                                url.searchParams.delete('newChat');
                                url.searchParams.set('conversationId', data.conversationId);
                                window.history.replaceState({}, '', url.toString());
                                refreshChatHistory(data.conversationId);
                            }
                            if (data.warning) {
                                showToast(data.warning);
                            }
                            if (data.suggestions && Array.isArray(data.suggestions) && data.suggestions.length > 0) {
                                updateFollowUpSuggestions(data.suggestions);
                            }
                        } catch (e) {
                            accumulatedMarkdown += dataStr;
                            bubbleContent.innerHTML = renderMarkdown(accumulatedMarkdown);
                            chatMessages.scrollTop = chatMessages.scrollHeight;
                        }
                    }
                }
            } catch (err) {
                console.error('Chat stream error:', err);
                bubbleEl.classList.add('error-bubble');
                bubbleContent.textContent = 'Lỗi kết nối khi gửi câu hỏi: ' + err.message + '. Vui lòng kiểm tra lại.';
            } finally {
                if (cursor) cursor.remove();
                if (accumulatedMarkdown) {
                    bubbleContent.innerHTML = renderMarkdown(accumulatedMarkdown);
                } else if (!bubbleEl.classList.contains('error-bubble') && !bubbleContent.textContent.trim()) {
                    bubbleEl.classList.add('error-bubble');
                    bubbleContent.textContent = 'Mô hình AI chưa phản hồi kịp hoặc gặp sự cố kết nối. Vui lòng bấm thử lại hoặc gửi lại câu hỏi.';
                }
                chatInput.disabled = false;
                if (sendButton) sendButton.disabled = false;
                chatInput.focus();
                chatMessages.scrollTop = chatMessages.scrollHeight;
            }
        });
    }

    const escapeHtml = (str) => {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    };

    const refreshChatHistory = async (activeId) => {
        if (!chatForm) return;
        const accessToken = chatForm.dataset.accessToken;
        if (!accessToken) return;
        const convsUrl = chatForm.dataset.conversationsUrl || (`/chat/${accessToken}/conversations`);
        const panelContent = document.querySelector('.chat-history-panel-content');
        const countBadges = document.querySelectorAll('.chat-history-count-badge');
        const csrfTokenEl = document.getElementById('chatCsrfToken');
        const convInput = chatForm.querySelector('input[name="conversationId"]');
        const currentActive = String(activeId || (convInput && convInput.value ? convInput.value : ''));

        try {
            const res = await fetch(convsUrl, {
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) return;
            const convs = await res.json();

            countBadges.forEach((badge) => {
                badge.textContent = convs ? convs.length : 0;
            });

            if (!panelContent) return;

            if (!convs || convs.length === 0) {
                panelContent.innerHTML = `
                    <div class="chat-history-empty">
                        <svg class="outline-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                        </svg>
                        <p>Chưa có phiên đàm đạo nào được lưu.</p>
                        <small>Hãy gửi câu hỏi đầu tiên để lưu lại cuộc đối thoại này!</small>
                    </div>
                `;
                return;
            }

            const listEl = document.createElement('ul');
            listEl.className = 'chat-history-list';

            convs.forEach((c) => {
                const isActive = String(c.id) === currentActive;
                const li = document.createElement('li');
                li.className = 'chat-history-item' + (isActive ? ' active' : '');

                const csrfField = csrfTokenEl
                    ? `<input type="hidden" name="${csrfTokenEl.name}" value="${csrfTokenEl.value}">`
                    : '';

                li.innerHTML = `
                    <a class="chat-history-link" href="/chat/${accessToken}?conversationId=${c.id}">
                        <div class="chat-history-item-top">
                            <span class="chat-history-title">${escapeHtml(c.title || ('Cuộc đàm đạo #' + c.id))}</span>
                            ${isActive ? '<span class="chat-history-current-tag">Đang xem</span>' : ''}
                        </div>
                        <div class="chat-history-meta">
                            <span class="chat-history-date">
                                <svg class="outline-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                <span>${escapeHtml(c.formattedTime || '')}</span>
                            </span>
                            <span class="chat-history-count">${c.messageCount || 0} tin</span>
                        </div>
                    </a>
                    <form class="chat-history-delete-form" method="post" action="/chat/${accessToken}/delete/${c.id}">
                        ${csrfField}
                        <button type="submit" class="chat-history-delete-btn" title="Xóa cuộc đàm đạo này">
                            <svg class="outline-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                        </button>
                    </form>
                `;
                listEl.appendChild(li);
            });

            panelContent.innerHTML = '';
            panelContent.appendChild(listEl);
        } catch (e) {
            console.warn('Could not refresh chat history', e);
        }
    };

    // Chat History Drawer toggle
    const chatHistoryDrawer = document.getElementById('chatHistoryDrawer');
    const openChatHistoryBtn = document.getElementById('openChatHistoryBtn');
    const closeChatHistoryBtn = document.getElementById('chatHistoryCloseBtn');
    const chatHistoryBackdrop = document.getElementById('chatHistoryBackdrop');

    const toggleChatHistory = (show) => {
        if (!chatHistoryDrawer) return;
        const willOpen = typeof show === 'boolean' ? show : !chatHistoryDrawer.classList.contains('open');
        chatHistoryDrawer.classList.toggle('open', willOpen);
        chatHistoryDrawer.setAttribute('aria-hidden', String(!willOpen));
        if (openChatHistoryBtn) {
            openChatHistoryBtn.setAttribute('aria-expanded', String(willOpen));
        }
    };

    if (openChatHistoryBtn) {
        openChatHistoryBtn.addEventListener('click', () => {
            const convInput = chatForm ? chatForm.querySelector('input[name="conversationId"]') : null;
            refreshChatHistory(convInput ? convInput.value : null);
            toggleChatHistory(true);
        });
    }
    if (closeChatHistoryBtn) {
        closeChatHistoryBtn.addEventListener('click', () => toggleChatHistory(false));
    }
    if (chatHistoryBackdrop) {
        chatHistoryBackdrop.addEventListener('click', () => toggleChatHistory(false));
    }

    // Delete Confirmation Modal Logic
    const deleteModal = document.getElementById('deleteConfirmModal');
    const deleteModalCancelBtn = document.getElementById('deleteModalCancelBtn');
    const deleteModalConfirmBtn = document.getElementById('deleteModalConfirmBtn');
    let pendingDeleteForm = null;

    const openDeleteModal = (form) => {
        if (!deleteModal) {
            if (confirm('Bạn có chắc muốn xóa cuộc đàm đạo này?')) {
                form.submit();
            }
            return;
        }
        pendingDeleteForm = form;
        deleteModal.classList.add('open');
        deleteModal.setAttribute('aria-hidden', 'false');
    };

    const closeDeleteModal = () => {
        if (!deleteModal) return;
        deleteModal.classList.remove('open');
        deleteModal.setAttribute('aria-hidden', 'true');
        pendingDeleteForm = null;
    };

    if (deleteModalCancelBtn) {
        deleteModalCancelBtn.addEventListener('click', closeDeleteModal);
    }
    if (deleteModal) {
        deleteModal.addEventListener('click', (e) => {
            if (e.target === deleteModal) closeDeleteModal();
        });
    }
    if (deleteModalConfirmBtn) {
        deleteModalConfirmBtn.addEventListener('click', () => {
            if (pendingDeleteForm) {
                deleteModalConfirmBtn.disabled = true;
                deleteModalConfirmBtn.textContent = 'Đang xóa...';
                pendingDeleteForm.submit();
            }
        });
    }

    // Intercept delete actions across both server-rendered and AJAX-rendered chat history
    document.addEventListener('click', (e) => {
        const deleteBtn = e.target.closest('.chat-history-delete-btn');
        if (deleteBtn) {
            e.preventDefault();
            e.stopPropagation();
            const form = deleteBtn.closest('form');
            if (!form) return;
            openDeleteModal(form);
        }
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (deleteModal && deleteModal.classList.contains('open')) {
                closeDeleteModal();
                return;
            }
            if (chatHistoryDrawer && chatHistoryDrawer.classList.contains('open')) {
                toggleChatHistory(false);
            }
        }
    });
})();
