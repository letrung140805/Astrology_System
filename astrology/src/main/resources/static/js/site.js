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

    document.querySelectorAll('.gender-choice input').forEach((radio) => {
        radio.addEventListener('change', () => {
            document.querySelectorAll('.gender-choice').forEach((choice) => choice.classList.toggle('selected', choice.contains(radio) && radio.checked));
        });
    });

    document.querySelectorAll('[data-toast]').forEach((button) => {
        button.addEventListener('click', (event) => {
            if (button.tagName === 'A') event.preventDefault();
            showToast(button.dataset.toast);
        });
    });

    document.querySelectorAll('[data-print]').forEach((button) => {
        button.addEventListener('click', () => window.print());
    });

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
            input.type = input.type === 'password' ? 'text' : 'password';
        });
    });

    document.querySelectorAll('[data-auth-tab]').forEach((button) => {
        button.addEventListener('click', () => {
            document.querySelectorAll('[data-auth-tab]').forEach((tab) => tab.classList.toggle('selected', tab === button));
            const register = button.dataset.authTab === 'register';
            const submit = document.querySelector('[data-login-submit]');
            const note = document.querySelector('[data-auth-note]');
            if (submit) submit.textContent = register ? 'Tạo tài khoản　→' : 'Đăng Nhập　→';
            if (note) note.textContent = register ? 'Đăng ký đang ở chế độ minh họa, chưa lưu thông tin.' : 'Giao diện minh họa · Tính năng tài khoản sẽ sớm ra mắt.';
        });
    });

    document.querySelectorAll('.auth-form, .account-form').forEach((form) => {
        form.addEventListener('submit', (event) => {
            event.preventDefault();
            showToast(form.matches('.auth-form') ? 'Đăng nhập chỉ là giao diện minh họa, chưa kết nối hệ thống tài khoản.' : 'Thông tin đã được cập nhật trong bản xem trước.');
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

        document.querySelectorAll('[data-prompt]').forEach((button) => {
            button.addEventListener('click', () => {
                if (chatInput) {
                    chatInput.value = button.dataset.prompt;
                    chatInput.focus();
                }
            });
        });

        const pinBtn = document.getElementById('pinPalaceBtn');
        if (pinBtn && chatInput) {
            pinBtn.addEventListener('click', () => {
                const tag = '[Ghim Cung Mệnh] ';
                if (!chatInput.value.includes(tag)) {
                    chatInput.value = tag + chatInput.value;
                }
                chatInput.focus();
            });
        }

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

            const userArticle = document.createElement('article');
            userArticle.className = 'message user-message';
            userArticle.innerHTML = `
                <div>
                    <div class="message-bubble"></div>
                    <small>Bạn</small>
                </div>
                <span class="chat-avatar">Bạn</span>
            `;
            userArticle.querySelector('.message-bubble').textContent = message;
            chatMessages.appendChild(userArticle);

            const aiArticle = document.createElement('article');
            aiArticle.className = 'message assistant';
            aiArticle.innerHTML = `
                <span class="chat-avatar">AI</span>
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
