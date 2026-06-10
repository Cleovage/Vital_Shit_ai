import { useEffect, useState } from "react";
import { collection, getDocs } from "firebase/firestore";
import { db } from "../lib/firebase";
import { ExternalLink, ShoppingBag, Loader2 } from "lucide-react";
import { motion } from "motion/react";

interface Product {
  id: string;
  heading: string;
  explanation: string;
  image: string;
  link: string;
}

export function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const querySnapshot = await getDocs(collection(db, "products"));
        const productsData = querySnapshot.docs.map((doc) => ({
          id: doc.id,
          ...doc.data(),
        })) as Product[];
        setProducts(productsData);
      } catch (err) {
        console.error("Error fetching products:", err);
        setError("Failed to load products. Please check your Firebase configuration and database rules.");
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-3 mb-2">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 text-white shadow-lg">
          <ShoppingBag className="h-6 w-6" />
        </div>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Products</h1>
          <p className="text-sm text-slate-500">Discover recommended items</p>
        </div>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-400">
          <Loader2 className="h-8 w-8 animate-spin mb-4 text-indigo-500" />
          <p className="font-medium">Loading products...</p>
        </div>
      ) : error ? (
        <div className="rounded-2xl bg-red-50 p-6 text-center text-red-600 border border-red-100 shadow-sm">
          <p className="font-medium">{error}</p>
          <p className="text-sm mt-2 opacity-80">Make sure your firebase.ts is configured and the 'products' collection exists.</p>
        </div>
      ) : products.length === 0 ? (
        <div className="rounded-2xl bg-slate-50 border border-slate-100 p-10 text-center text-slate-500 shadow-sm">
          <ShoppingBag className="h-12 w-12 mx-auto mb-4 opacity-20" />
          <p className="font-medium text-lg text-slate-700 mb-1">No products found</p>
          <p className="text-sm">Add items to your Firebase database to see them here.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 auto-rows-[260px]">
          {products.map((product, index) => {
            // Create the bento grid variety
            // Every 3rd item is large
            const isLarge = index % 3 === 0;

            return (
              <motion.a
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1, duration: 0.4, ease: [0.25, 0.4, 0, 1] }}
                key={product.id}
                href={product.link || "#"}
                target="_blank"
                rel="noopener noreferrer"
                className={`group relative overflow-hidden rounded-[2rem] bg-white shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 transition-all duration-300 hover:shadow-[0_20px_40px_rgb(0,0,0,0.12)] hover:-translate-y-1 ${
                  isLarge ? "row-span-2 md:col-span-2" : "row-span-1"
                }`}
              >
                {/* Background Image */}
                <div className="absolute inset-0 w-full h-full bg-slate-100">
                  <img
                    src={product.image || "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80"}
                    alt={product.heading}
                    className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105"
                  />
                  {/* Gradient Overlay */}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-black/10" />
                </div>

                {/* Content */}
                <div className="absolute bottom-0 left-0 right-0 p-6 sm:p-8 flex flex-col justify-end h-full">
                  <div className="flex justify-between items-end gap-6">
                    <div className="flex-1">
                      <h3 className={`font-bold text-white mb-2 leading-tight ${isLarge ? "text-3xl" : "text-2xl"}`}>
                        {product.heading}
                      </h3>
                      <p className={`text-white/80 text-sm leading-relaxed ${isLarge ? "line-clamp-4" : "line-clamp-2"}`}>
                        {product.explanation}
                      </p>
                    </div>
                    
                    <div className="flex-shrink-0 h-12 w-12 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center text-white border border-white/30 group-hover:bg-white group-hover:text-black transition-all duration-300">
                      <ExternalLink className="h-5 w-5" />
                    </div>
                  </div>
                </div>
              </motion.a>
            );
          })}
        </div>
      )}
    </div>
  );
}
