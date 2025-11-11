let stompClient = null;
let currentFolderId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // currentFolderId를 HTML에서 가져오기
    const folderIdElement = document.getElementById('currentFolderId');
    if (folderIdElement) {
        const value = folderIdElement.value;
        currentFolderId = value === '' ? null : parseInt(value);
    }

    // WebSocket 연결
    connect();

    // 이벤트 리스너 등록
    initializeEventListeners();
});

// WebSocket 연결
function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/drive', function(message) {
            const data = JSON.parse(message.body);
            handleRealtimeUpdate(data);
        });
    });
}

// 실시간 업데이트 처리
function handleRealtimeUpdate(data) {
    if (data.folderId === currentFolderId || (data.folderId === null && currentFolderId === null)) {
        showNotification(getUpdateMessage(data.type));
        setTimeout(() => location.reload(), 1000);
    }
}

function getUpdateMessage(type) {
    switch(type) {
        case 'FILE_UPLOADED': return '새 파일이 업로드되었습니다.';
        case 'FILE_DELETED': return '파일이 삭제되었습니다.';
        case 'FOLDER_CREATED': return '새 폴더가 생성되었습니다.';
        case 'FOLDER_DELETED': return '폴더가 삭제되었습니다.';
        default: return '변경사항이 있습니다.';
    }
}

// 드롭다운 토글
function toggleDropdown() {
    const dropdown = document.getElementById('dropdownMenu');
    const container = document.querySelector('.breadcrumb-dropdown');

    dropdown.classList.toggle('active');
    container.classList.toggle('active');
}

function closeDropdown() {
    const dropdown = document.getElementById('dropdownMenu');
    const container = document.querySelector('.breadcrumb-dropdown');

    dropdown.classList.remove('active');
    container.classList.remove('active');
}

// 모달 관련
function openUploadModal() {
    document.getElementById('uploadModal').classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeUploadModal() {
    document.getElementById('uploadModal').classList.remove('active');
    document.getElementById('uploadForm').reset();
    document.getElementById('selectedFiles').style.display = 'none';
    document.body.style.overflow = 'auto';
}

function openFolderModal() {
    document.getElementById('folderModal').classList.add('active');
    document.body.style.overflow = 'hidden';
    setTimeout(() => document.getElementById('folderName').focus(), 100);
}

function closeFolderModal() {
    document.getElementById('folderModal').classList.remove('active');
    document.getElementById('folderForm').reset();
    document.body.style.overflow = 'auto';
}

function closeModalOnBackdrop(event, modalId) {
    if (event.target.id === modalId) {
        if (modalId === 'uploadModal') closeUploadModal();
        if (modalId === 'folderModal') closeFolderModal();
    }
}

// 파일 크기 포맷팅
function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
}

// 폴더 이동
function navigateToFolder(folderId) {
    window.location.href = '/?folderId=' + folderId;
}

// 파일 다운로드
function downloadFile(fileId) {
    window.location.href = '/api/files/' + fileId + '/download';
}

// 파일 삭제
async function deleteFile(fileId) {
    if (!confirm('이 파일을 삭제하시겠습니까?')) return;

    try {
        await fetch('/api/files/' + fileId, {
            method: 'DELETE'
        });
        showNotification('파일이 삭제되었습니다.');
        setTimeout(() => location.reload(), 1000);
    } catch (error) {
        console.error('Delete error:', error);
        showNotification('삭제 중 오류가 발생했습니다.');
    }
}

// 폴더 삭제
async function deleteFolder(folderId) {
    if (!confirm('이 폴더와 모든 내용을 삭제하시겠습니까?')) return;

    try {
        await fetch('/api/folders/' + folderId, {
            method: 'DELETE'
        });
        showNotification('폴더가 삭제되었습니다.');
        setTimeout(() => location.reload(), 1000);
    } catch (error) {
        console.error('Delete error:', error);
        showNotification('삭제 중 오류가 발생했습니다.');
    }
}

// 알림 표시
function showNotification(message) {
    const notification = document.getElementById('notification');
    notification.textContent = message;
    notification.classList.add('active');
    setTimeout(() => {
        notification.classList.remove('active');
    }, 3000);
}

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', function(event) {
        const container = document.querySelector('.breadcrumb-dropdown');
        if (container && !container.contains(event.target)) {
            closeDropdown();
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeUploadModal();
            closeFolderModal();
            closeDropdown();
        }
    });

    // 파일 선택 감지
    const fileInput = document.getElementById('fileInput');
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const files = e.target.files;
            if (files.length > 0) {
                const fileList = document.getElementById('fileList');
                fileList.innerHTML = '';
                for (let file of files) {
                    const div = document.createElement('div');
                    div.className = 'selected-file-item';
                    div.textContent = `📄 ${file.name} (${formatFileSize(file.size)})`;
                    fileList.appendChild(div);
                }
                document.getElementById('selectedFiles').style.display = 'block';
            }
        });
    }

    // 파일 업로드 폼
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const fileInput = document.getElementById('fileInput');
            const files = fileInput.files;

            if (files.length === 0) {
                showNotification('파일을 선택해주세요.');
                return;
            }

            for (let file of files) {
                const formData = new FormData();
                formData.append('file', file);
                if (currentFolderId) {
                    formData.append('folderId', currentFolderId);
                }

                try {
                    await fetch('/api/files/upload', {
                        method: 'POST',
                        body: formData
                    });
                } catch (error) {
                    console.error('Upload error:', error);
                    showNotification('업로드 중 오류가 발생했습니다.');
                    return;
                }
            }

            closeUploadModal();
            showNotification('파일이 업로드되었습니다.');
            setTimeout(() => location.reload(), 1000);
        });
    }

    // 폴더 생성 폼
    const folderForm = document.getElementById('folderForm');
    if (folderForm) {
        folderForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const folderName = document.getElementById('folderName').value.trim();

            if (!folderName) {
                showNotification('폴더 이름을 입력해주세요.');
                return;
            }

            const formData = new FormData();
            formData.append('folderName', folderName);
            if (currentFolderId) {
                formData.append('parentId', currentFolderId);
            }

            try {
                await fetch('/api/folders', {
                    method: 'POST',
                    body: formData
                });
                closeFolderModal();
                showNotification('폴더가 생성되었습니다.');
                setTimeout(() => location.reload(), 1000);
            } catch (error) {
                console.error('Folder creation error:', error);
                showNotification('폴더 생성 중 오류가 발생했습니다.');
            }
        });
    }

    // 드래그 앤 드롭
    const uploadArea = document.getElementById('uploadArea');
    if (uploadArea) {
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('drag-over');
        });

        uploadArea.addEventListener('dragleave', () => {
            uploadArea.classList.remove('drag-over');
        });

        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('drag-over');
            const dt = e.dataTransfer;
            const files = dt.files;
            document.getElementById('fileInput').files = files;

            // 파일 목록 표시
            const event = new Event('change');
            document.getElementById('fileInput').dispatchEvent(event);
        });
    }
}