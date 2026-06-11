import { Link } from "react-router-dom";
import { ArrowRight, Clock, Award, CheckCircle, Presentation, CreditCard } from "lucide-react";
import { mockCourses } from "../data/mock";

export default function Home() {
  const coursesExcerpt = mockCourses.filter(c => c.popular).slice(0, 3);

  return (
    <div className="w-full">
      {/* Hero Section */}
      <section className="relative min-h-[700px] flex items-center justify-center overflow-hidden bg-surface">
        <div className="absolute inset-0 opacity-10 bg-cover bg-center" style={{ backgroundImage: "url('https://lh3.googleusercontent.com/aida-public/AB6AXuD7PWlVAcldvfEx26YgbZXfULhZZp_FxJltnFBTYXPr02bdwfVxMevIrL4n4XOefNj1kYVDmTl7E28C3iWvoqdZy25FRx_4lQ-KfAF_iaPpvD0Wj7rGYmIDJwsRDldKfSDmm2JRRIbvgUGdd0dIWBjJQRQkvUNVNvQDOh0oIrs3YQ2xl8zw8RNzAV-YUUNpjamxsafnYYZ3ILfEjE1lB8mKJYootRE0LBnzIuwAtb4eZWPGWI6QMAVIZcCmI-A_MeZ0rHUUJeftEw8V')" }}></div>
        <div className="relative z-10 max-w-7xl mx-auto px-6 text-center flex flex-col items-center">
          <span className="inline-flex items-center px-4 py-1.5 rounded-full bg-primary-container/10 text-primary font-medium text-sm mb-6 border border-primary/20 shadow-sm">
            <Award className="w-4 h-4 mr-2" /> 卓越教育
          </span>
          <h1 className="text-4xl md:text-5xl lg:text-5xl font-extrabold text-foreground mb-6 max-w-4xl tracking-tight leading-tight">
            培养全球领导力人才
          </h1>
          <p className="text-lg md:text-xl text-muted mb-12 max-w-2xl mx-auto leading-relaxed">
            卓越学术机构提供由世界一流教员指导的严谨、系统设计的课程。在无干扰、学术氛围浓厚的环境中提升您的潜力。
          </p>
          <div className="flex flex-col sm:flex-row items-center gap-4">
            <Link to="/courses" className="px-8 py-3.5 bg-primary text-primary-foreground rounded-lg font-semibold text-lg hover:bg-primary/90 transition-all shadow-md flex items-center group">
              探索课程
              <ArrowRight className="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </Link>
            <Link to="/courses" className="px-8 py-3.5 bg-transparent text-primary border border-border rounded-lg font-semibold text-lg hover:border-primary transition-all hover:bg-surface-dim">
              查看课程表
            </Link>
          </div>
        </div>
      </section>

      {/* Advantages Section */}
      <section className="py-24 bg-surface-dim">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-foreground mb-4">我们的优势</h2>
            <p className="text-base text-muted max-w-2xl mx-auto">专为无妥协的学术成功而设计的框架。</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {[
              { icon: Presentation, title: "专业师资", desc: "直接向行业领导者和杰出的终身教授学习。" },
              { icon: CheckCircle, title: "系统化课程", desc: "逻辑严密的课程体系确保全面掌握学科知识。" },
              { icon: CreditCard, title: "透明定价", desc: "清晰、预先支付的学费，无任何隐藏的机构费用。" },
              { icon: CheckCircle, title: "轻松入学", desc: "简化、顺畅的申请流程，全程在线管理。" }
            ].map((feature, i) => (
              <div key={i} className="bg-surface p-8 rounded-xl border border-border hover:shadow-lg transition-all group">
                <div className="w-12 h-12 rounded-lg bg-surface-dim flex items-center justify-center mb-6 text-primary group-hover:bg-primary group-hover:text-white transition-colors">
                  <feature.icon className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-foreground mb-2">{feature.title}</h3>
                <p className="text-sm text-muted">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Popular Courses Section */}
      <section className="py-24 bg-surface">
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex justify-between items-end mb-12">
            <div>
              <h2 className="text-3xl font-bold text-foreground mb-2">热门课程</h2>
              <p className="text-muted">本学期最受欢迎的课程。</p>
            </div>
            <Link to="/courses" className="hidden md:flex items-center text-primary font-medium hover:underline">
              查看全部 <ArrowRight className="ml-1 w-5 h-5" />
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {coursesExcerpt.map((course) => (
              <Link to="/courses" key={course.id} className="bg-surface rounded-xl border border-border overflow-hidden hover:shadow-lg transition-all flex flex-col group block">
                <div className="h-48 overflow-hidden relative">
                  <img src={course.image} alt={course.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div className="absolute top-4 right-4 px-2 py-1 bg-surface/90 backdrop-blur text-primary font-bold text-xs rounded shadow-sm">
                    {course.format}
                  </div>
                </div>
                <div className="p-6 flex-1 flex flex-col">
                  <h3 className="text-lg font-bold text-foreground mb-2 leading-tight">{course.title}</h3>
                  <p className="text-sm text-muted mb-6 line-clamp-2 flex-grow">{course.description}</p>
                  <div className="flex justify-between items-center pt-4 border-t border-border">
                    <span className="font-bold text-lg text-foreground">{course.price}</span>
                    <ArrowRight className="w-5 h-5 text-muted group-hover:text-primary transition-colors" />
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
