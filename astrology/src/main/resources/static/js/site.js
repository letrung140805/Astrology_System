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

    const chatForm = document.querySelector('[data-chat-form]');
    if (chatForm) {
        const messages = document.querySelector('[data-chat-messages]');
        const sendMessage = (text) => {
            const cleanText = text.trim();
            if (!cleanText) return;
            const message = document.createElement('article');
            message.className = 'message user-message';
            const content = document.createElement('div');
            const bubble = document.createElement('div');
            bubble.className = 'message-bubble';
            bubble.textContent = cleanText;
            const timestamp = document.createElement('small');
            timestamp.textContent = 'Bạn • Vừa xong';
            content.append(bubble, timestamp);
            const avatar = document.createElement('span');
            avatar.className = 'chat-avatar';
            avatar.textContent = 'Lan';
            message.append(content, avatar);
            messages.append(message);
            chatForm.querySelector('input').value = '';
            messages.scrollTop = messages.scrollHeight;
            showToast('Đây là bản xem trước; trợ lý AI chưa kết nối dịch vụ trả lời.');
        };
        chatForm.addEventListener('submit', (event) => {
            event.preventDefault();
            sendMessage(chatForm.querySelector('input').value);
        });
        document.querySelectorAll('[data-prompt]').forEach((button) => {
            button.addEventListener('click', () => sendMessage(button.dataset.prompt));
        });
    }
})();
