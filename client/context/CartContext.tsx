"use client";

import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";

export interface CartItem {
  productId: number;
  title: string;
  price: number;
  thumbnail: string;
  quantity: number;
  stock: number; // 담을 때 재고를 같이 저장해둬서, 그 이상은 못 담게(수량 조절도) 막는 데 씀
}

interface CartContextValue {
  items: CartItem[];
  addItem: (item: Omit<CartItem, "quantity">, quantity?: number) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  totalCount: number;
  totalPrice: number;
}

const CartContext = createContext<CartContextValue | null>(null);
const STORAGE_KEY = "kiosk_cart";

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);
  const [hydrated, setHydrated] = useState(false);

  // 첫 마운트 시 localStorage에서 읽어옴
  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      try {
        setItems(JSON.parse(raw));
      } catch {
        // 저장된 값이 깨져있으면 그냥 빈 장바구니로 시작
      }
    }
    setHydrated(true);
  }, []);

  // items가 바뀔 때마다 localStorage에 저장
  // hydrated 되기 전(아직 localStorage 안 읽어옴)엔 저장 안 함 - 빈 배열로 덮어쓰는 거 방지
  useEffect(() => {
    if (!hydrated) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items, hydrated]);

  const addItem = (item: Omit<CartItem, "quantity">, quantity = 1) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.productId === item.productId);
      if (existing) {
        // 재고는 매번 최신값(item.stock)으로 갱신 - 담아둔 사이에 재고가 바뀌었을 수 있어서
        const cappedQuantity = Math.min(
          existing.quantity + quantity,
          item.stock
        );
        return prev.map((i) =>
          i.productId === item.productId
            ? { ...i, stock: item.stock, quantity: cappedQuantity }
            : i
        );
      }
      return [...prev, { ...item, quantity: Math.min(quantity, item.stock) }];
    });
  };

  const removeItem = (productId: number) => {
    setItems((prev) => prev.filter((i) => i.productId !== productId));
  };

  const updateQuantity = (productId: number, quantity: number) => {
    if (quantity <= 0) {
      removeItem(productId);
      return;
    }
    setItems((prev) =>
      prev.map((i) =>
        i.productId === productId
          ? {
              ...i,
              // 예전에 담긴 카트 데이터엔 stock이 없을 수 있어서(이 필드 추가 전) 방어적으로 처리
              quantity:
                typeof i.stock === "number"
                  ? Math.min(quantity, i.stock)
                  : quantity,
            }
          : i
      )
    );
  };

  const clearCart = () => setItems([]);

  const totalCount = items.reduce((sum, i) => sum + i.quantity, 0);
  const totalPrice = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  return (
    <CartContext.Provider
      value={{
        items,
        addItem,
        removeItem,
        updateQuantity,
        clearCart,
        totalCount,
        totalPrice,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) {
    throw new Error("useCart는 CartProvider 안에서만 사용할 수 있습니다.");
  }
  return ctx;
}
