/**
 * ListHelper - Tiện ích hỗ trợ quản lý trạng thái list, phân trang, sắp xếp và gọi API.
 */
const ListHelper = {
    state: {
        page: 0,
        size: 20,
        keyword: "",
        sortBy: "id",
        sortDir: "desc",
        additionalParams: {}
    },
    apiUrl: "",
    onRender: null, // Function(data)

    init(apiUrl, onRenderFn, defaultSortBy = "id", defaultSortDir = "desc") {
        this.apiUrl = apiUrl;
        this.onRender = onRenderFn;
        this.state.sortBy = defaultSortBy;
        this.state.sortDir = defaultSortDir;
        
        // Khôi phục trạng thái từ URL
        this.restoreStateFromUrl();
        this.load();
        this.bindEvents();
    },

    bindEvents() {
        // Tìm kiếm (debounce)
        const searchInput = document.getElementById("searchKeyword");
        if (searchInput) {
            searchInput.value = this.state.keyword;
            searchInput.addEventListener("input", this.debounce((e) => {
                this.setKeyword(e.target.value);
            }, 500));
        }

        // Chọn size
        const sizeSelect = document.getElementById("pageSizeSelect");
        if (sizeSelect) {
            sizeSelect.value = this.state.size;
            sizeSelect.addEventListener("change", (e) => {
                this.setSize(parseInt(e.target.value));
            });
        }
    },

    async load() {
        try {
            this.showLoading();
            const url = this.buildUrl();
            this.updateUrlParams();
            
            const response = await window.apiFetch(url);
            if (this.onRender) {
                this.onRender(response);
            }
            this.renderPagination(response);
            this.updateSortHeaders();
        } catch (error) {
            console.error("Lỗi tải danh sách:", error);
            this.showError();
        }
    },

    buildUrl() {
        const params = new URLSearchParams();
        params.append("page", this.state.page);
        params.append("size", this.state.size);
        if (this.state.keyword) params.append("keyword", this.state.keyword);
        if (this.state.sortBy) params.append("sortBy", this.state.sortBy);
        if (this.state.sortDir) params.append("sortDir", this.state.sortDir);
        
        // Thêm các param filter khác
        for (const [key, value] of Object.entries(this.state.additionalParams)) {
            if (value !== "" && value !== null && value !== undefined) {
                params.append(key, value);
            }
        }
        
        return `${this.apiUrl}?${params.toString()}`;
    },

    updateUrlParams() {
        const params = new URLSearchParams();
        params.append("page", this.state.page);
        params.append("size", this.state.size);
        if (this.state.keyword) params.append("keyword", this.state.keyword);
        if (this.state.sortBy) params.append("sortBy", this.state.sortBy);
        if (this.state.sortDir) params.append("sortDir", this.state.sortDir);
        
        for (const [key, value] of Object.entries(this.state.additionalParams)) {
            if (value !== "" && value !== null && value !== undefined) {
                params.append(key, value);
            }
        }
        
        const newUrl = `${window.location.pathname}?${params.toString()}`;
        window.history.replaceState({}, "", newUrl);
    },

    restoreStateFromUrl() {
        const params = new URLSearchParams(window.location.search);
        if (params.has("page")) this.state.page = parseInt(params.get("page"));
        if (params.has("size")) this.state.size = parseInt(params.get("size"));
        if (params.has("keyword")) this.state.keyword = params.get("keyword");
        if (params.has("sortBy")) this.state.sortBy = params.get("sortBy");
        if (params.has("sortDir")) this.state.sortDir = params.get("sortDir");
    },

    setKeyword(kw) {
        this.state.keyword = kw;
        this.state.page = 0; // Reset page
        this.load();
    },

    setPage(p) {
        this.state.page = p;
        this.load();
    },

    setSize(s) {
        this.state.size = s;
        this.state.page = 0; // Reset page
        this.load();
    },

    setSort(by) {
        if (this.state.sortBy === by) {
            this.state.sortDir = this.state.sortDir === "asc" ? "desc" : "asc";
        } else {
            this.state.sortBy = by;
            this.state.sortDir = "asc";
        }
        this.load();
    },
    
    setFilter(key, value) {
        this.state.additionalParams[key] = value;
        this.state.page = 0;
        this.load();
    },

    debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    },

    renderPagination(pageData) {
        const pageInfo = document.getElementById("pageInfo");
        const btnPrev = document.getElementById("btnPrevPage");
        const btnNext = document.getElementById("btnNextPage");

        if (pageInfo) {
            pageInfo.textContent = `Trang ${pageData.page + 1} / ${pageData.totalPages} (Tổng: ${pageData.totalElements})`;
        }
        
        if (btnPrev) {
            btnPrev.disabled = pageData.first;
            btnPrev.onclick = () => this.setPage(this.state.page - 1);
        }
        
        if (btnNext) {
            btnNext.disabled = pageData.last;
            btnNext.onclick = () => this.setPage(this.state.page + 1);
        }
    },

    updateSortHeaders() {
        document.querySelectorAll(".sortable-header").forEach(th => {
            const by = th.getAttribute("data-sort");
            th.classList.remove("text-blue-600");
            const icon = th.querySelector(".sort-icon");
            if (icon) {
                if (this.state.sortBy === by) {
                    th.classList.add("text-blue-600");
                    icon.textContent = this.state.sortDir === "asc" ? "↑" : "↓";
                } else {
                    icon.textContent = "↕";
                }
            }
        });
    },
    
    showLoading() {
        const tbody = document.getElementById("tableBody");
        if (tbody) {
            const cols = tbody.closest("table").querySelectorAll("th").length;
            tbody.innerHTML = `<tr><td colspan="${cols}" class="text-center py-8 text-gray-500">Đang tải dữ liệu...</td></tr>`;
        }
    },
    
    showError() {
        const tbody = document.getElementById("tableBody");
        if (tbody) {
            const cols = tbody.closest("table").querySelectorAll("th").length;
            tbody.innerHTML = `<tr><td colspan="${cols}" class="text-center py-8 text-red-500">Lỗi tải dữ liệu. Vui lòng thử lại.</td></tr>`;
        }
    }
};

window.ListHelper = ListHelper;
