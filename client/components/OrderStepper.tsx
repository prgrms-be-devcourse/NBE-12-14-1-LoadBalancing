const STEPS = ["메뉴 선택", "주문확인", "결제하기", "결제 완료"];

export default function OrderStepper({ currentStep }: { currentStep: number }) {
  return (
    <div className="mb-8 flex items-center justify-center gap-2">
      {STEPS.map((label, index) => {
        const step = index + 1;
        const isActive = step === currentStep;
        const isDone = step < currentStep;

        return (
          <div key={label} className="flex items-center gap-2">
            <div className="flex items-center gap-2">
              <span
                className={`flex h-7 w-7 items-center justify-center rounded-full text-label-sm font-bold ${
                  isActive
                    ? "bg-black text-white"
                    : isDone
                      ? "bg-gray-300 text-white"
                      : "bg-gray-100 text-gray-400"
                }`}
              >
                {step}
              </span>
              <span
                className={`text-label-sm uppercase tracking-wide ${
                  isActive ? "font-bold text-black" : "text-gray-400"
                }`}
              >
                {label}
              </span>
            </div>
            {step < STEPS.length && <span className="h-px w-8 bg-gray-200" />}
          </div>
        );
      })}
    </div>
  );
}
