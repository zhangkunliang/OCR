"""
OCR证件识别分类脚本
支持身份证、营业执照等证件类型的识别和分类
支持单个文件或目录批量处理
"""

import sys
import json
import re
import traceback
import os
import glob
import cv2
import numpy as np
from paddleocr import PaddleOCR

class DocumentClassifier:
    """证件类型分类器"""

    def __init__(self):
        """初始化分类器"""
        self.ocr = None
        self.init_ocr()

    def init_ocr(self):
        """初始化OCR"""
        try:
            # 使用更简单的配置，提高识别效果
            self.ocr = PaddleOCR(lang='ch')
        except Exception as e:
            raise Exception(f"初始化PaddleOCR失败: {str(e)}")

    def preprocess_image(self, image_path):
        """预处理图像以提高OCR识别效果"""
        try:
            # 读取图像
            img = cv2.imread(image_path)
            if img is None:
                return image_path  # 如果无法读取，返回原路径

            # 转换为灰度图
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

            # 自适应直方图均衡化
            clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
            enhanced = clahe.apply(gray)

            # 高斯滤波去噪
            denoised = cv2.GaussianBlur(enhanced, (3, 3), 0)

            # 保存预处理后的图像
            base_name = os.path.splitext(image_path)[0]
            processed_path = f"{base_name}_processed.jpg"
            cv2.imwrite(processed_path, denoised)

            return processed_path
        except Exception as e:
            print(f"图像预处理失败: {e}")
            return image_path  # 如果预处理失败，返回原路径

    def classify_document_type(self, ocr_result):
        """根据OCR识别结果判断文档类型"""
        if not ocr_result or len(ocr_result) == 0:
            return "未知类型", "低"

        # 提取所有文本内容
        text_content = ""
        for line in ocr_result:
            if line and len(line) > 1:
                try:
                    # 检查line[1]的结构
                    if isinstance(line[1], (list, tuple)) and len(line[1]) > 0:
                        text_content += str(line[1][0]) + " "
                    else:
                        text_content += str(line[1]) + " "
                except (IndexError, TypeError) as e:
                    # 如果访问失败，跳过这一行
                    continue

        text_content = text_content.upper()

        # 身份证识别规则 - 包含更多可能的字符组合
        identity_keywords = [
            '居民身份证', '中华人民共和国', '身份证', '出生', '性别', '民族', '住址',
            '姓名', '公民', '街道', '名', '别', '生', '址', '道', '民'
        ]
        identity_score = sum(1 for keyword in identity_keywords if keyword in text_content)

        # 18位身份证号码正则
        id_number_pattern = r'\d{17}[\dX]'
        if re.search(id_number_pattern, text_content):
            identity_score += 3

        # 营业执照识别规则
        license_keywords = [
            '营业执照', '统一社会信用代码', '法定代表人', '注册资本', '成立日期',
            '营业期限', '经营范围', '住所', '类型'
        ]
        license_score = sum(1 for keyword in license_keywords if keyword in text_content)

        # 统一社会信用代码正则 (18位)
        credit_code_pattern = r'[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}'
        if re.search(credit_code_pattern, text_content):
            license_score += 3

        # 驾驶证识别规则
        driving_keywords = [
            '驾驶证', '机动车驾驶证', '准驾车型', '有效期限', '初次领证日期'
        ]
        driving_score = sum(1 for keyword in driving_keywords if keyword in text_content)

        # 护照识别规则
        passport_keywords = [
            '护照', 'PASSPORT', '中华人民共和国护照', '签发机关', '签发日期'
        ]
        passport_score = sum(1 for keyword in passport_keywords if keyword in text_content)

        # 根据得分判断类型
        scores = {
            '身份证': identity_score,
            '营业执照': license_score,
            '驾驶证': driving_score,
            '护照': passport_score
        }

        # 找到最高分的类型
        max_score = max(scores.values())
        if max_score == 0:
            return "未知类型", "低"

        document_type = max(scores, key=scores.get)

        # 根据得分确定置信度
        if max_score >= 3:
            confidence = "高"
        elif max_score >= 2:
            confidence = "中"
        else:
            confidence = "低"

        return document_type, confidence

    def classify_document_type_from_texts(self, rec_texts):
        """根据rec_texts列表判断文档类型"""
        if not rec_texts:
            return "未知类型"

        # 将所有文本合并
        text_content = ' '.join(rec_texts).upper()

        # 身份证识别规则
        identity_keywords = [
            '居民身份证', '中华人民共和国', '身份证', '出生', '性别', '民族', '住址',
            '公民身份号码','签发机关','有效期限'
        ]
        identity_score = sum(1 for keyword in identity_keywords if keyword in text_content)

        # 18位身份证号码正则
        id_number_pattern = r'\d{17}[\dX]'
        if re.search(id_number_pattern, text_content):
            identity_score += 3

        # 营业执照识别规则
        license_keywords = [
            '营业执照', '统一社会信用代码', '法定代表人', '注册资本', '成立日期',
            '营业期限', '经营范围', '住所', '类型'
        ]
        license_score = sum(1 for keyword in license_keywords if keyword in text_content)

        # 统一社会信用代码正则 (18位)
        credit_code_pattern = r'[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}'
        if re.search(credit_code_pattern, text_content):
            license_score += 3

        # 驾驶证识别规则
        driving_keywords = [
            '驾驶证', '机动车驾驶证', '准驾车型', '有效期限', '初次领证日期'
        ]
        driving_score = sum(1 for keyword in driving_keywords if keyword in text_content)

        # 护照识别规则
        passport_keywords = [
            '护照', 'PASSPORT', '中华人民共和国护照', '签发机关', '签发日期'
        ]
        passport_score = sum(1 for keyword in passport_keywords if keyword in text_content)

        # 根据得分判断类型
        scores = {
            '身份证': identity_score,
            '营业执照': license_score,
            '驾驶证': driving_score,
            '护照': passport_score
        }

        # 找到最高分的类型
        max_score = max(scores.values())
        if max_score == 0:
            return "未知类型"

        return max(scores, key=scores.get)

    def save_result_to_output(self, image_path, result):
        """将结果保存到output文件夹"""
        try:
            # 创建output文件夹（如果不存在）
            output_dir = "output"
            if not os.path.exists(output_dir):
                os.makedirs(output_dir)

            # 生成输出文件名
            base_name = os.path.splitext(os.path.basename(image_path))[0]
            output_filename = f"{base_name}_classification_result.json"
            output_path = os.path.join(output_dir, output_filename)

            # 添加时间戳和图像路径信息
            enhanced_result = {
                "timestamp": self.get_current_timestamp(),
                "input_image": image_path,
                "result": result
            }

            # 保存JSON文件
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(enhanced_result, f, ensure_ascii=False, indent=2)

            print(f"结果已保存到: {output_path}")

        except Exception as e:
            print(f"保存结果失败: {e}")

    def save_result_to_output_silent(self, image_path, result):
        """静默将结果保存到output文件夹（不输出日志）"""
        try:
            # 创建output文件夹（如果不存在）
            output_dir = "output"
            if not os.path.exists(output_dir):
                os.makedirs(output_dir)

            # 生成输出文件名
            base_name = os.path.splitext(os.path.basename(image_path))[0]
            output_filename = f"{base_name}_classification_result.json"
            output_path = os.path.join(output_dir, output_filename)

            # 添加时间戳和图像路径信息
            enhanced_result = {
                "timestamp": self.get_current_timestamp(),
                "input_image": image_path,
                "result": result
            }

            # 保存JSON文件
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(enhanced_result, f, ensure_ascii=False, indent=2)

        except Exception as e:
            pass  # 静默处理错误

    def get_current_timestamp(self):
        """获取当前时间戳"""
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    def extract_key_info(self, ocr_result, document_type):
        """根据证件类型提取关键信息"""
        key_info = {}

        if not ocr_result:
            return key_info

        text_content = ""
        for line in ocr_result:
            if line and len(line) > 1:
                try:
                    # 检查line[1]的结构
                    if isinstance(line[1], (list, tuple)) and len(line[1]) > 0:
                        text_content += str(line[1][0]) + " "
                    else:
                        text_content += str(line[1]) + " "
                except (IndexError, TypeError):
                    # 如果访问失败，跳过这一行
                    continue

        if document_type == "身份证":
            # 提取身份证关键信息
            name_pattern = r'姓名[：:\s]*([^\s]+)'
            id_pattern = r'(\d{17}[\dX])'

            name_match = re.search(name_pattern, text_content)
            if name_match:
                key_info['姓名'] = name_match.group(1)

            id_match = re.search(id_pattern, text_content)
            if id_match:
                key_info['身份证号'] = id_match.group(1)

        elif document_type == "营业执照":
            # 提取营业执照关键信息
            name_pattern = r'名称[：:\s]*([^\n]+)'
            credit_pattern = r'([0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10})'

            name_match = re.search(name_pattern, text_content)
            if name_match:
                key_info['企业名称'] = name_match.group(1).strip()

            credit_match = re.search(credit_pattern, text_content)
            if credit_match:
                key_info['统一社会信用代码'] = credit_match.group(1)

        return key_info

    def process_image(self, image_path):
        """处理图片并返回识别结果"""
        try:
            # 预处理图像
            processed_path = self.preprocess_image(image_path)

            # 执行OCR识别
            results = self.ocr.predict(input = processed_path)
            for res in results:
                res.save_to_img("output")
            # 清理临时文件
            if processed_path != image_path and os.path.exists(processed_path):
                try:
                    os.remove(processed_path)
                except:
                    pass

            if not results:
                return {
                    "error": "未能识别到任何文字内容"
                }

            # 从JSON格式的results中提取rec_texts
            rec_texts = []
            if isinstance(results, dict) and 'rec_texts' in results:
                rec_texts = results['rec_texts']
            elif isinstance(results, list) and len(results) > 0:
                if isinstance(results[0], dict) and 'rec_texts' in results[0]:
                    rec_texts = results[0]['rec_texts']
                else:
                    # 如果不是预期的格式，尝试从旧格式中提取
                    ocr_result = results[0]
                    for line in ocr_result:
                        if line and len(line) >= 2:
                            try:
                                if isinstance(line[1], (list, tuple)) and len(line[1]) >= 1:
                                    text = str(line[1][0])
                                else:
                                    text = str(line[1])
                                if text.strip():
                                    rec_texts.append(text.strip())
                            except (IndexError, TypeError, ValueError):
                                continue
            else:
                return {
                    "error": "OCR结果格式不正确"
                }

            if not rec_texts:
                return {
                    "error": "未能识别到任何文字内容"
                }

            # 基于rec_texts进行证件类型分类
            document_type = self.classify_document_type_from_texts(rec_texts)

            # 构建最终结果
            final_result = {
                'document_type': document_type,
                'rec_texts': rec_texts
            }

            # 静默保存结果到output文件夹（不输出日志）
            self.save_result_to_output_silent(image_path, final_result)

            # 返回简化的结果，只包含document_type和rec_texts
            return final_result


        except Exception as e:
            error_result = {
                "error": f"处理图片时发生错误: {str(e)}"
            }
            # 即使出错也保存结果
            self.save_result_to_output_silent(image_path, error_result)
            return error_result

def get_image_files(path):
    """获取图片文件列表"""
    # 支持的图片格式
    image_extensions = ['*.jpg', '*.jpeg', '*.png', '*.bmp', '*.tiff', '*.webp']

    image_files = []

    if os.path.isfile(path):
        # 如果是文件，直接返回
        image_files.append(path)
    elif os.path.isdir(path):
        # 如果是目录，搜索所有图片文件
        for ext in image_extensions:
            pattern = os.path.join(path, ext)
            image_files.extend(glob.glob(pattern))
            # 也搜索大写扩展名
            pattern_upper = os.path.join(path, ext.upper())
            image_files.extend(glob.glob(pattern_upper))
    else:
        raise FileNotFoundError(f"路径不存在: {path}")

    return sorted(image_files)

def process_single_image(classifier, image_path):
    """处理单个图片"""
    result = classifier.process_image(image_path)
    result['image_path'] = image_path
    return result

def main():
    """主函数"""
    # 设置标准输出编码为UTF-8
    import sys
    import codecs
    if sys.stdout.encoding != 'utf-8':
        sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')

    if len(sys.argv) != 2:
        print(json.dumps({
            "error": "用法: python ocr_classifier.py <image_path_or_directory>"
        }, ensure_ascii=False))
        sys.exit(1)

    input_path = sys.argv[1]

    try:
        # 获取图片文件列表
        image_files = get_image_files(input_path)

        if not image_files:
            print(json.dumps({
                "error": f"在路径 {input_path} 中未找到任何图片文件"
            }, ensure_ascii=False))
            sys.exit(1)

        # 初始化分类器
        classifier = DocumentClassifier()

        results = []

        # 处理每个图片
        for image_path in image_files:
            try:
                result = process_single_image(classifier, image_path)
                results.append(result)
            except Exception as e:
                error_result = {
                    "image_path": image_path,
                    "error": f"处理图片失败: {str(e)}"
                }
                results.append(error_result)

        # 输出结果
        if len(results) == 1:
            # 单个文件，只输出核心结果（document_type和rec_texts）
            result = results[0]
            if 'error' not in result:
                core_result = {
                    "document_type": result.get("document_type"),
                    "rec_texts": result.get("rec_texts", [])
                }
                print(json.dumps(core_result, ensure_ascii=False))
            else:
                # 如果有错误，输出完整的错误信息
                print(json.dumps(result, ensure_ascii=False))
        else:
            # 多个文件，输出汇总结果
            output = {
                "total_processed": len(results),
                "results": results
            }
            print(json.dumps(output, ensure_ascii=False))

    except Exception as e:
        error_msg = f"程序执行错误: {str(e)}"
        print(json.dumps({
            "error": error_msg,
            "traceback": traceback.format_exc()
        }, ensure_ascii=False))
        sys.exit(1)

if __name__ == "__main__":
    main()