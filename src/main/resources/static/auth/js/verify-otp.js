document.addEventListener("DOMContentLoaded", function() {
    let timeLeft = 120; // 120 seconds = 2 minutes
    const countdownEl = document.getElementById("countdown");
    const resendBtn = document.getElementById("resendBtn");

    // Only start countdown if the elements exist
    if (countdownEl && resendBtn) {
        const timerId = setInterval(() => {
            if (timeLeft <= 0) {
                clearInterval(timerId);
                countdownEl.innerHTML = "Đã hết hạn";
                resendBtn.disabled = false;
                resendBtn.classList.remove("text-muted");
            } else {
                let m = Math.floor(timeLeft / 60);
                let s = timeLeft % 60;
                m = m < 10 ? "0" + m : m;
                s = s < 10 ? "0" + s : s;
                countdownEl.innerHTML = m + ":" + s;
                timeLeft -= 1;
            }
        }, 1000);
    }
});