function closeModal() {
    document.querySelectorAll(".modal").forEach(modal => {
        modal.style.display = "none";
    });
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".close-btn").forEach(btn => {
        btn.addEventListener("click", closeModal);
    });

    const anyVisibleModal = Array.from(document.querySelectorAll(".modal"))
        .some(modal => getComputedStyle(modal).display !== "none");

    if (anyVisibleModal) {
        setTimeout(closeModal, 5000);
    }
});
