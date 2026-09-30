/**
 * Quản lý tương tác Layout: Mobile Sidebar Toggle & Menu Active
 */

document.addEventListener("DOMContentLoaded", function () {
    // 1. Toggle Mobile Sidebar
    const btnHamburger = document.getElementById("btn-hamburger");
    const btnCloseSidebar = document.getElementById("btn-close-sidebar");
    const sidebar = document.getElementById("sidebar");
    const sidebarOverlay = document.getElementById("sidebar-overlay");

    function openSidebar() {
        if (sidebar) {
            sidebar.classList.remove("-translate-x-full");
        }
        if (sidebarOverlay) {
            sidebarOverlay.classList.remove("hidden");
        }
    }

    function closeSidebar() {
        if (sidebar) {
            sidebar.classList.add("-translate-x-full");
        }
        if (sidebarOverlay) {
            sidebarOverlay.classList.add("hidden");
        }
    }

    if (btnHamburger) {
        btnHamburger.addEventListener("click", openSidebar);
    }
    if (btnCloseSidebar) {
        btnCloseSidebar.addEventListener("click", closeSidebar);
    }
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener("click", closeSidebar);
    }

    // 2. Highlight Active Menu Item dựa theo đường dẫn URL
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll("#sidebar nav a");

    menuLinks.forEach(link => {
        const href = link.getAttribute("href");
        if (href) {
            let isActive = false;
            // Exact match
            if (currentPath === href) {
                isActive = true;
            } 
            // Prefix match (nhưng bỏ qua các root url của role)
            else if (href !== '/' && href !== '/admin' && href !== '/teacher' && href !== '/student' && currentPath.startsWith(href)) {
                isActive = true;
            }

            if (isActive) {
                link.classList.add("bg-blue-600", "text-white", "font-semibold");
                link.classList.remove("text-gray-300", "hover:bg-gray-700");
            }
        }
    });

    // 3. Load user info vào Header nếu có trong localStorage
    if (window.authService) {
        const user = window.authService.getCurrentUser();
        if (user) {
            const headerName = document.getElementById("header-user-fullname");
            const headerRole = document.getElementById("header-user-role");
            const headerAvatar = document.getElementById("header-user-avatar");

            if (headerName) headerName.textContent = user.fullName || user.username;
            if (headerRole) headerRole.textContent = user.role;
            if (headerAvatar && user.avatarUrl) headerAvatar.src = user.avatarUrl;

            // Hiển thị menu sidebar tương ứng với Role
            if (user.role === 'ADMIN') {
                const el = document.getElementById('menu-admin');
                if (el) el.classList.remove('hidden');
            } else if (user.role === 'TEACHER') {
                const el = document.getElementById('menu-teacher');
                if (el) el.classList.remove('hidden');
            } else if (user.role === 'STUDENT') {
                const el = document.getElementById('menu-student');
                if (el) el.classList.remove('hidden');
            }
        }
    }
});
