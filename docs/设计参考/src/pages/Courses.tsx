import { Search, Clock } from "lucide-react";
import { mockCourses } from "../data/mock";
import { cn } from "../lib/utils";

export default function Courses() {
  const categories = ["全部课程", "计算机科学", "工商管理", "人文艺术", "数据分析", "医疗管理", "设计"];

  return (
    <div className="w-full max-w-7xl mx-auto px-6 py-12 md:py-16">
      <div className="mb-10">
        <h1 className="text-3xl md:text-4xl font-bold text-foreground mb-4">探索课程</h1>
        <p className="text-lg text-muted max-w-2xl">发现专为卓越学术和专业荣誉而设计的课程。</p>
      </div>

      <div className="flex flex-col md:flex-row gap-6 mb-12">
        <div className="flex-grow md:max-w-md relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted w-5 h-5" />
          <input 
            type="text" 
            placeholder="搜索课程、讲师或主题..." 
            className="w-full pl-10 pr-4 py-3 bg-surface-dim border border-border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-shadow outline-none text-sm"
          />
        </div>
        <div className="flex overflow-x-auto scrollbar-hide space-x-2 py-1 md:py-0 items-center">
          {categories.map((cat, i) => (
            <button 
              key={cat} 
              className={cn(
                "px-5 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors",
                i === 0 
                  ? "bg-primary text-primary-foreground" 
                  : "bg-surface border border-border text-foreground hover:bg-surface-dim"
              )}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-16">
        {mockCourses.map((course) => (
          <article key={course.id} className="bg-surface border border-border rounded-xl overflow-hidden hover:shadow-lg transition-all duration-300 group flex flex-col">
            <div className="relative h-48 overflow-hidden">
              <img src={course.image} alt={course.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
              <div className="absolute top-4 right-4 bg-surface/90 backdrop-blur-sm px-2.5 py-1 rounded text-xs text-primary font-bold shadow-sm">
                {course.format}
              </div>
            </div>
            
            <div className="p-6 flex flex-col flex-grow">
              <div className="flex items-center space-x-3 mb-4">
                <span className="px-2 py-1 bg-surface-dim text-foreground rounded text-[10px] font-bold tracking-wider uppercase">
                  {course.category}
                </span>
                <span className="text-muted text-xs flex items-center font-medium">
                  <Clock className="w-3.5 h-3.5 mr-1" /> {course.duration}
                </span>
              </div>
              
              <h3 className="text-xl font-bold text-foreground mb-3 leading-tight">{course.title}</h3>
              <p className="text-sm text-muted mb-6 flex-grow">{course.description}</p>
              
              <div className="mt-auto pt-4 border-t border-border flex justify-between items-center">
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-full bg-primary-container text-white flex items-center justify-center font-bold text-xs">
                    {course.instructor.initials}
                  </div>
                  <span className="text-sm font-medium text-foreground">{course.instructor.name}</span>
                </div>
                <div className="text-lg text-primary font-bold">
                  {course.price}
                </div>
              </div>
            </div>
          </article>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex justify-center items-center space-x-2">
        <button disabled className="w-10 h-10 flex items-center justify-center rounded border border-border text-muted opacity-50 bg-surface-dim">
          &lt;
        </button>
        <button className="w-10 h-10 flex items-center justify-center rounded bg-primary text-white font-medium text-sm">1</button>
        <button className="w-10 h-10 flex items-center justify-center rounded border border-border text-foreground hover:bg-surface-dim transition-colors text-sm font-medium">2</button>
        <button className="w-10 h-10 flex items-center justify-center rounded border border-border text-foreground hover:bg-surface-dim transition-colors text-sm font-medium">3</button>
        <span className="px-2 text-muted">...</span>
        <button className="w-10 h-10 flex items-center justify-center rounded border border-border text-foreground hover:bg-surface-dim transition-colors text-sm font-medium">8</button>
        <button className="w-10 h-10 flex items-center justify-center rounded border border-border text-foreground hover:bg-surface-dim transition-colors">
          &gt;
        </button>
      </div>
    </div>
  );
}
